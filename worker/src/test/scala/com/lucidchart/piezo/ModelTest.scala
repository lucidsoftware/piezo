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
import java.io.InputStream
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ModelTest extends Specification with BeforeAll with AfterAll {
  sequential

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
    println("--------- AFTER ALL --------------")
  }

  override def beforeAll(): Unit = {
    println("--------- BEFORE ALL --------------")
    val piezoSchema = for (num <- 0 to 9) yield getPatchFile(s"piezo_mysql_$num.sql")
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

  /**
   * Run a body with a connection provider available
   */
  private def withConnectionProvider[T](
    failoverEveryConnection: Boolean = false,
  )(body: (() => java.sql.Connection) => T): T = {
    val provider = new PiezoConnectionProvider(
      dbUrl,
      "com.mysql.cj.jdbc.Driver",
      username,
      password,
      supportIPFailover = true,
      causeFailoverEveryConnection = failoverEveryConnection,
    )

    try {
      body(() => provider.getConnection())
    } finally {
      provider.shutdown()
    }
  }

  "JobHistoryModel" should {
    "work correctly" in withConnectionProvider() { getConnection =>
      val jobHistoryModel = new JobHistoryModel(getConnection)
      val jobKey = new JobKey("blah", "blah")
      val triggerKey = new TriggerKey("blahtn", "blahtg")
      jobHistoryModel.getJobs().isEmpty must beTrue
      jobHistoryModel.addJob("ab", jobKey, triggerKey, Instant.now(), 1000, true)
      jobHistoryModel.getJob(jobKey).headOption must beSome
      jobHistoryModel.getLastJobSuccessByTrigger(triggerKey) must beSome
      jobHistoryModel.getJobs().nonEmpty must beTrue

      // Delete the remaining record, so it doesn't affect other tests
      val connection = getConnection()
      val prepared = connection.prepareStatement(s"""DELETE FROM job_history""")
      prepared.executeUpdate()
      connection.close()

      jobHistoryModel.getJob(jobKey).toSet mustEqual Set.empty
    }

    "work correctly with a failover for every connection to the database" in withConnectionProvider(
      failoverEveryConnection = true,
    ) { getConnection =>
      val jobHistoryModel = new JobHistoryModel(getConnection)
      val jobKey = new JobKey("blahc", "blahc")
      val triggerKey = new TriggerKey("blahtnc", "blahtgc")
      jobHistoryModel.getJob(jobKey).headOption must beNone
      jobHistoryModel.addJob("abc", jobKey, triggerKey, Instant.now(), 1000, true)
      jobHistoryModel.getJob(jobKey).headOption must beSome
      jobHistoryModel.getLastJobSuccessByTrigger(triggerKey) must beSome

      // Delete the remaining record, so it doesn't affect other tests
      val connection = getConnection()
      val prepared = connection.prepareStatement(s"""DELETE FROM job_history""")
      prepared.executeUpdate()
      connection.close()

      jobHistoryModel.getJob(jobKey).toSet mustEqual Set.empty
    }
  }

  "TriggerMonitoringModel" should {
    "work correctly" in withConnectionProvider() { getConnection =>
      val triggerMonitoringPriorityModel = new TriggerMonitoringModel(getConnection)
      val triggerKey = new TriggerKey("blahz", "blahz")
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

  "JobHistoryCleanup" should {
    "cleanup only non-permanent records" in withConnectionProvider() { getConnection =>
      val jobHistoryModel = new JobHistoryModel(getConnection)
      val temporaryTriggerKey = new TriggerKey("blahjz", "blahzg")
      val jobKey = new JobKey("blahjz123", "blahzg123")
      val scheduledStart = java.time.Instant.now()
      val temporaryFireInstanceId = "FireInstanceId"
      val permanentFireInstanceId = 123456789
      val permanentFireInstanceIdString = permanentFireInstanceId.toString
      jobHistoryModel.addJob(temporaryFireInstanceId, jobKey, temporaryTriggerKey, scheduledStart, 1, true)
      jobHistoryModel.addOneTimeJobIfNotExists(jobKey, permanentFireInstanceId)

      jobHistoryModel
        .getJob(jobKey)
        .map(_.fire_instance_id)
        .toSet mustEqual Set(temporaryFireInstanceId, permanentFireInstanceIdString)
      jobHistoryModel.deleteJobs(Instant.now().plusSeconds(3)) mustEqual 1
      jobHistoryModel
        .getJob(jobKey)
        .map(_.fire_instance_id)
        .toSet mustEqual Set(permanentFireInstanceIdString)

      // Delete the remaining record, so it doesn't affect other tests
      val connection = getConnection()
      val prepared = connection.prepareStatement(s"""DELETE FROM job_history""")
      prepared.executeUpdate()
      connection.close()

      jobHistoryModel.getJob(jobKey).toSet mustEqual Set.empty
    }

    "only triggers job once, when given the same fireInstanceId" in withConnectionProvider() { getConnection =>
      given scala.concurrent.ExecutionContext = global

      val jobHistoryModel = new JobHistoryModel(getConnection)
      val jobKey = new JobKey("blahjzasd", "blahzgasd")
      val fireInstanceId: Long = 123123123

      val combinedFutures: Future[Set[Boolean]] = Future.sequence(
        Set(
          Future {
            jobHistoryModel.addOneTimeJobIfNotExists(jobKey, fireInstanceId)
          },
          Future {
            jobHistoryModel.addOneTimeJobIfNotExists(jobKey, fireInstanceId)
          },
        ),
      )

      // Doesn't matter which one inserted the record, as long as one did, and one didn't
      Await.result(combinedFutures, Duration.Inf) mustEqual Set(true, false)

      // Truncate to the second, so that we don't end up with a rounding
      // error when we do the comparison.
      // Otherwise, on insert, the second might be rounded up, and we end up a second
      // of from adding one second to the actual date time, and truncating it.
      val fireTime = java.time.Instant.now().truncatedTo(SECONDS)
      val instanceDurationInMillis: Long = 3000
      jobHistoryModel.completeOneTimeJob(
        fireInstanceId.toString,
        fireTime,
        instanceDurationInMillis,
        true,
      )

      // Verify that only one of the one-time-jobs was added to the table, and that it was "completed" with the correct time
      // Calculate expected finish time the same way completeOneTimeJob does to avoid rounding issues
      val expectedFinishSeconds = fireTime.plusMillis(instanceDurationInMillis).getEpochSecond
      jobHistoryModel
        .getJob(jobKey)
        .map(record => (record.fire_instance_id, record.finish.map(_.getEpochSecond))) must containTheSameElementsAs(
        List(
          (
            fireInstanceId.toString,
            Some(expectedFinishSeconds),
          ),
        ),
      )
      jobHistoryModel.deleteJobs(Instant.now().plusSeconds(3)) mustEqual 0

      // Delete the remaining record, so it doesn't affect other tests
      val connection = getConnection()
      val prepared = connection.prepareStatement(s"""DELETE FROM job_history""")
      prepared.executeUpdate()
      connection.close()

      jobHistoryModel.getJob(jobKey).toSet mustEqual Set.empty
    }
  }

  "TriggerHistoryModel" should {
    "work correctly" in withConnectionProvider() { getConnection =>
      val triggerHistoryModel = new TriggerHistoryModel(getConnection)
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
      triggerHistoryModel.deleteTriggers(Instant.now().plusSeconds(1)) mustEqual 1
      val triggerKey2 = new TriggerKey("blahj2", "blahg")
      triggerHistoryModel.addTrigger(
        triggerKey2,
        triggerFireTime = None,
        actualStart = Some(Instant.now()),
        misfire = true,
        fireInstanceId = Some("blah"),
      )
      val newRecord = triggerHistoryModel.getTrigger(triggerKey2).head
      newRecord.actual_start must beSome
      newRecord.fire_instance_id mustEqual "blah"

      triggerHistoryModel.deleteTriggers(Instant.now().plusSeconds(1)) mustEqual 1
    }
  }
}
