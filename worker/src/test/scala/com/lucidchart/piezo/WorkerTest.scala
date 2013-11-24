package com.lucidchart.piezo

import org.specs2.mutable._
import org.quartz._
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz.SimpleScheduleBuilder._
import org.quartz.impl.StdSchedulerFactory
import java.util.Properties

object WorkerStopJob {
  var runCount = 0
}

class WorkerStopJob extends Job {
  def execute(context: JobExecutionContext) {
    println("upping semaphore")
    WorkerStopJob.runCount += 1
    Worker.runSemaphore.release()
  }
}

class WorkerTest extends Specification{
  "worker run" should {
    "stop" in {
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
      val schedulerFactory = new StdSchedulerFactory(properties)
      val scheduler = schedulerFactory.getScheduler
      scheduler.scheduleJob(job, trigger)
      Worker.run(scheduler)
      println("worker stopped")
      WorkerStopJob.runCount must equalTo(1)
    }
  }
}
