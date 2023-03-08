package com.lucidchart.piezo

import java.io.{BufferedReader, FileReader, File}
import java.nio.file.Files
import java.nio.file.Paths
import org.specs2.mutable._
import org.specs2.specification._
import org.quartz._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.SimpleScheduleBuilder._
import org.quartz.impl.StdSchedulerFactory
import java.sql.{Connection,DriverManager}
import java.util.Properties
import scala.jdk.CollectionConverters._
import scala.util.Random
import java.util.Date
import org.quartz.{Calendar, Job, JobDataMap, JobDetail, JobExecutionContext, Scheduler, Trigger}


class ModelTest extends Specification with BeforeAll with AfterAll {

    val username = "root"
    val password = "root"
    Class.forName("com.mysql.cj.jdbc.Driver")
    override def afterAll(): Unit = {
        val newConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306", username, password)
        val statement = newConnection.createStatement
        statement.executeUpdate("DROP DATABASE IF EXISTS test_jobs")
        newConnection.close()
    }

    override def beforeAll(): Unit = {
        val paths = for (num <- 0 to 7) yield Paths.get(getClass.getResource(s"/piezo_mysql_$num.sql").toURI())
        val schema = (List(Paths.get(getClass.getResource(s"/quartz_mysql_0.sql").toURI())) ++ paths).map { path => 
            Files.readAllLines(path).asScala.mkString("\n")
        }.mkString("\n").split(";")
        val connection:Connection = DriverManager.getConnection("jdbc:mysql://localhost:3306", username, password)
        val testDb = "test_jobs"
        try {
            val statement = connection.createStatement
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS test_jobs")
            statement.close()
            connection.close()
            val newConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test_jobs", username, password)
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
            // setupDB()
          val propertiesStream = getClass().getResourceAsStream("/quartz_test_mysql.properties")
          val properties = new Properties
          properties.load(propertiesStream)
          properties.setProperty("org.quartz.scheduler.instanceName", "testScheduler" + Random.nextInt())
          val jobHistoryModel = new JobHistoryModel(properties)
          jobHistoryModel.addJob("ab", "blah", "blah", "blah", "blah", new Date(), 1000, true)
          jobHistoryModel.getJob("blah", "blah").headOption must beSome
          jobHistoryModel.getLastJobSuccessByTrigger("blah", "blah") must beSome
          jobHistoryModel.getJobs()
            true mustEqual true
        }
    }
}