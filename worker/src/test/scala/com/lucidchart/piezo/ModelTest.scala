package com.lucidchart.piezo

import java.nio.file.Files
import java.nio.file.Paths
import org.quartz.{JobKey, TriggerKey}
import org.specs2.mutable.*
import org.specs2.specification.*
import java.sql.DriverManager
import java.util.Properties
import scala.jdk.CollectionConverters.*
import scala.util.Using
import java.util.Date
import java.io.InputStream

class ModelTest extends Specification with BeforeAll with AfterAll {
  val propertiesStream: InputStream = getClass().getResourceAsStream("/quartz_test_mysql.properties")
  val properties = new Properties
  properties.load(propertiesStream)

  val propertiesStreamFailoverEveryConnection: InputStream =
    getClass().getResourceAsStream("/quartz_test_mysql_failover_every_connection.properties")

  val username: String = properties.getProperty("org.quartz.dataSource.test_jobs.user")
  val password: String = properties.getProperty("org.quartz.dataSource.test_jobs.password")
  val dbUrl: String = properties.getProperty("org.quartz.dataSource.test_jobs.URL")
  val urlParts :+ testDb = dbUrl.split("/").toSeq: @unchecked
  val mysqlUrl: String = urlParts.mkString("/")
  Class.forName("com.mysql.cj.jdbc.Driver")

  private def getPatchFile(fileName: String) = {
    Paths.get(getClass.getResource(s"/$fileName").toURI())
  }

  private def runSql(dbUrl: String, sql: String) = {
    Using.resource(DriverManager.getConnection(dbUrl, username, password)) { connection =>
      Using.resource(connection.createStatement) { statement =>
        statement.executeUpdate(sql)
      }
    }
  }

  override def afterAll(): Unit = {
    runSql(mysqlUrl, s"DROP DATABASE IF EXISTS $testDb")
  }

  override def beforeAll(): Unit = {
    val piezoSchema = for (num <- 0 to 8) yield getPatchFile(s"piezo_mysql_$num.sql")
    val quartzSchema = getPatchFile("quartz_mysql_0.sql")
    val schema = (quartzSchema +: piezoSchema)
      .map { path =>
        Files.readAllLines(path).asScala.mkString("\n")
      }
      .mkString("\n")
      .split(";")

    runSql(mysqlUrl, s"CREATE DATABASE IF NOT EXISTS $testDb")

    for (s <- schema) {
      runSql(dbUrl, s)
    }
  }

  private def getConnectionProvider(failoverEveryConnection: Boolean = false): () => java.sql.Connection = {
    val provider = new PiezoConnectionProvider(
      dbUrl,
      "com.mysql.cj.jdbc.Driver",
      username,
      password,
      supportIPFailover = true,
      causeFailoverEveryConnection = failoverEveryConnection,
    )

    () => provider.getConnection()
  }

  "JobHistoryModel" should {
    "work correctly" in {
      val jobHistoryModel = new JobHistoryModel(getConnectionProvider())
      val jobKey = new JobKey("blah", "blah")
      val triggerKey = new TriggerKey("blahtn", "blahtg")
      jobHistoryModel.getJobs().isEmpty must beTrue
      jobHistoryModel.addJob("ab", jobKey, triggerKey, new Date(), 1000, true)
      jobHistoryModel.getJob(jobKey).headOption must beSome
      jobHistoryModel.getLastJobSuccessByTrigger(triggerKey) must beSome
      jobHistoryModel.getJobs().nonEmpty must beTrue
    }

    "work correctly with a failover for every connection to the database" in {
      val jobHistoryModel = new JobHistoryModel(getConnectionProvider(true))
      val jobKey = new JobKey("blahc", "blahc")
      val triggerKey = new TriggerKey("blahtnc", "blahtgc")
      jobHistoryModel.getJob(jobKey).headOption must beNone
      jobHistoryModel.addJob("abc", jobKey, triggerKey, new Date(), 1000, true)
      jobHistoryModel.getJob(jobKey).headOption must beSome
      jobHistoryModel.getLastJobSuccessByTrigger(triggerKey) must beSome
    }
  }

  "TriggerMonitoringModel" should {
    "work correctly" in {
      val triggerMonitoringPriorityModel = new TriggerMonitoringModel(getConnectionProvider())
      val triggerKey = new TriggerKey("blahj", "blahg")
      triggerMonitoringPriorityModel.getTriggerMonitoringRecord(triggerKey) must beNone
      triggerMonitoringPriorityModel.setTriggerMonitoringRecord(
        triggerKey,
        TriggerMonitoringPriority.Low,
        1800,
        Some("my-team"),
      )
      triggerMonitoringPriorityModel.getTriggerMonitoringRecord(triggerKey) must beSome
      triggerMonitoringPriorityModel.deleteTriggerMonitoringRecord(triggerKey) mustEqual 1
      triggerMonitoringPriorityModel.getTriggerMonitoringRecord(triggerKey) must beNone
    }
  }

  "TriggerHistoryModel" should {
    "work correctly" in {
      val triggerHistoryModel = new TriggerHistoryModel(getConnectionProvider())
      val triggerKey = new TriggerKey("blahj", "blahg")
      triggerHistoryModel.addTrigger(
        triggerKey,
        triggerFireTime = None,
        actualStart = None,
        misfire = true,
        fireInstanceId = None,
      )
      val insertedRecord = triggerHistoryModel.getTrigger(triggerKey).head
      insertedRecord.actual_start must beNone
      insertedRecord.fire_instance_id mustEqual ""
      // increase the time by 1 second so that the condition for the test satisfies.
      triggerHistoryModel.deleteTriggers(new Date().getTime + 1000) mustEqual 1
      val triggerKey2 = new TriggerKey("blahj2", "blahg")
      triggerHistoryModel.addTrigger(
        triggerKey2,
        triggerFireTime = None,
        actualStart = Some(new Date()),
        misfire = true,
        fireInstanceId = Some("blah"),
      )
      val newRecord = triggerHistoryModel.getTrigger(triggerKey2).head
      newRecord.actual_start must beSome
      newRecord.fire_instance_id mustEqual "blah"

      triggerHistoryModel.deleteTriggers(new Date().getTime + 1000) mustEqual 1
    }
  }
}
