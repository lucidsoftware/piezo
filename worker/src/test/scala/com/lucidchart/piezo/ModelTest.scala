package com.lucidchart.piezo


import java.nio.file.Files
import java.nio.file.Paths
import org.specs2.mutable._
import org.specs2.specification._
import java.sql.{Connection,DriverManager}
import java.util.Properties
import scala.jdk.CollectionConverters._
import scala.util.Random
import java.util.Date

class ModelTest extends Specification with BeforeAll with AfterAll {
    val propertiesStream = getClass().getResourceAsStream("/quartz_test_mysql.properties")
    val properties = new Properties
    properties.load(propertiesStream)

    val username = properties.getProperty("org.quartz.dataSource.test_jobs.user")
    val password = properties.getProperty("org.quartz.dataSource.test_jobs.password")
    val dbUrl = properties.getProperty("org.quartz.dataSource.test_jobs.URL")
    val testDb = dbUrl.split("/").last
    val mysqlUrl = dbUrl.split("/").dropRight(1).mkString("/")
    Class.forName("com.mysql.cj.jdbc.Driver")

    override def afterAll(): Unit = {
        val newConnection = DriverManager.getConnection(mysqlUrl, username, password)
        val statement = newConnection.createStatement
        statement.executeUpdate(s"DROP DATABASE IF EXISTS $testDb")
        newConnection.close()
    }

    override def beforeAll(): Unit = {
        val paths = for (num <- 0 to 7) yield Paths.get(getClass.getResource(s"/piezo_mysql_$num.sql").toURI())
        val schema = (List(Paths.get(getClass.getResource(s"/quartz_mysql_0.sql").toURI())) ++ paths).map { path => 
            Files.readAllLines(path).asScala.mkString("\n")
        }.mkString("\n").split(";")
        val connection:Connection = DriverManager.getConnection(mysqlUrl, username, password)
        try {
            val statement = connection.createStatement
            statement.executeUpdate(s"CREATE DATABASE IF NOT EXISTS $testDb")
            statement.close()
            connection.close()
            val newConnection = DriverManager.getConnection(dbUrl, username, password)
            for (s <- schema) {
                val stmt =  newConnection.createStatement
                stmt.executeUpdate(s)
                stmt.close()    
            }
            newConnection.close()
        } catch {
            case e: Exception => e.printStackTrace
        }
    }

    "JobHistoryModel" should {
        "work correctly" in {
          properties.setProperty("org.quartz.scheduler.instanceName", "testScheduler" + Random.nextInt())
          val jobHistoryModel = new JobHistoryModel(properties)
          jobHistoryModel.addJob("ab", "blah", "blah", "blah", "blah", new Date(), 1000, true)
          jobHistoryModel.getJob("blah", "blah").headOption must beSome
          jobHistoryModel.getLastJobSuccessByTrigger("blah", "blah") must beSome
          jobHistoryModel.getJobs()
        }
    }
}