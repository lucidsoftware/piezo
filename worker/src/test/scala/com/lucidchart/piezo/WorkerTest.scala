package com.lucidchart.piezo

import java.io.{BufferedReader, FileReader, File}

import org.specs2.mutable.*
import org.quartz.*
import org.quartz.JobBuilder.*
import org.quartz.TriggerBuilder.*
import org.quartz.SimpleScheduleBuilder.*
import org.quartz.impl.StdSchedulerFactory
import java.util.Properties

import scala.util.Random


object WorkerStopJob {
  var runCount = 0
}

class WorkerStopJob() extends Job {
  def execute(context: JobExecutionContext): Unit = {
    println("upping semaphore")
    WorkerStopJob.runCount += 1
    Worker.runSemaphore.release()
  }
}

class WorkerTest extends Specification {
  sequential
  "worker run" should {
    "stop" in {
      Worker.runSemaphore.drainPermits()
      val job = newJob((new WorkerStopJob).getClass)
        .withIdentity("job1", "group1")
        .build()

      val trigger = newTrigger()
        .withIdentity("trigger1", "group1")
        .startNow()
        .withSchedule(simpleSchedule()
        .withIntervalInSeconds(5)
        .repeatForever())
        .build()

      val propertiesStream = getClass().getResourceAsStream("/quartz_test.properties")
      val properties = new Properties
      properties.load(propertiesStream)
      properties.setProperty("org.quartz.scheduler.instanceName", "testScheduler" + Random.nextInt())
      val schedulerFactory = new StdSchedulerFactory(properties)
      val scheduler = schedulerFactory.getScheduler
      scheduler.scheduleJob(job, trigger)
      Worker.run(scheduler, properties)
      println("worker stopped")
      WorkerStopJob.runCount must equalTo(1)
    }

    "write heartbeat timestamp" in {
      Worker.runSemaphore.drainPermits()
      val job = newJob((new WorkerStopJob).getClass)
        .withIdentity("job2", "group2")
        .build()

      val trigger = newTrigger()
        .withIdentity("trigger2", "group2")
        .startNow()
        .withSchedule(simpleSchedule()
        .withIntervalInSeconds(1)
        .repeatForever())
        .build()

      val propertiesStream = getClass().getResourceAsStream("/quartz_test.properties")
      val properties = new Properties
      properties.load(propertiesStream)

      val heartbeatFilePath = "/tmp/piezo/piezoHeartbeatTest" + Random.nextInt()
      properties.setProperty("com.lucidchart.piezo.heartbeatFile", heartbeatFilePath)

      println("running worker")
      properties.setProperty("org.quartz.scheduler.instanceName", "testScheduler" + Random.nextInt())
      val schedulerFactory = new StdSchedulerFactory(properties)
      val scheduler = schedulerFactory.getScheduler
      scheduler.scheduleJob(job, trigger)
      Worker.run(scheduler, properties, 1, 3)
      println("worker stopped")

      val heartbeatFile = new File(heartbeatFilePath)
      val exists = heartbeatFile.exists()
      exists must equalTo(true)
      println("heartbeat file exists")

      val reader = new BufferedReader(new FileReader(heartbeatFile))
      val heartbeat = reader.readLine()
      reader.close()
      println("heartbeat timestamp: " + heartbeat)
      val heartbeatTime = Worker.dtf.parseDateTime(heartbeat.trim)
      val inRange = heartbeatTime.isAfter(System.currentTimeMillis() - 5 * 1000)

      inRange must equalTo(true)
    }
  }
}
