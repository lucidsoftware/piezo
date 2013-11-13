package com.lucidchart.piezo.admin.controllers

import org.quartz.{SimpleScheduleBuilder, TriggerBuilder, JobBuilder, Scheduler}
import com.lucidchart.piezo.jobs.monitoring.HeartBeat

/**
  */
object TestUtil {
  val jobGroup = "testJobGroup"
  val jobName = "testJobName"
  val triggerGroup = "testTriggerGroup"
  val triggerName = "testTriggerName"

  def createJob(scheduler: Scheduler) = {
    val jobDetail = JobBuilder.newJob(classOf[HeartBeat]).withIdentity(jobName, jobGroup).build()
    val trigger = TriggerBuilder.newTrigger
      .withIdentity(triggerName, triggerGroup)
      .withSchedule(
      SimpleScheduleBuilder.simpleSchedule
        .withIntervalInSeconds(5)
        .withRepeatCount(1))
      .build()
    scheduler.deleteJob(jobDetail.getKey())
    scheduler.scheduleJob(jobDetail, trigger)
  }
}
