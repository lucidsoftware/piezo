package com.lucidchart.piezo.admin.controllers

import com.lucidchart.piezo.TriggerMonitoringPriority
import com.lucidchart.piezo.TriggerMonitoringPriority.TriggerMonitoringPriority
import java.text.ParseException
import org.quartz._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

class TriggerFormHelper(scheduler: Scheduler) extends JobDataHelper {

  private def simpleScheduleFormApply(repeatCount: Int, repeatInterval: Int): SimpleScheduleBuilder = {
    SimpleScheduleBuilder.simpleSchedule()
      .withRepeatCount(repeatCount)
      .withIntervalInSeconds(repeatInterval)
  }

  private def simpleScheduleFormUnapply(simple: SimpleScheduleBuilder) = {
    val simpleTrigger = simple.build().asInstanceOf[SimpleTrigger]
    Some((simpleTrigger.getRepeatCount, simpleTrigger.getRepeatInterval.toInt / 1000))
  }

  private def cronScheduleFormApply(cronExpression: String): CronScheduleBuilder = {
    CronScheduleBuilder.cronSchedule(cronExpression)
  }

  private def cronScheduleFormUnapply(cron: CronScheduleBuilder) = {
    val cronTrigger = cron.build().asInstanceOf[CronTrigger]
    Some((cronTrigger.getCronExpression()))
  }

  private def triggerFormApply(
    triggerType: String,
    group: String,
    name: String,
    jobGroup: String,
    jobName: String,
    description: String,
    simple: Option[SimpleScheduleBuilder],
    cron: Option[CronScheduleBuilder],
    jobDataMap: Option[JobDataMap],
    triggerMonitoringPriority: String,
    triggerMaxErrorTime: Int
  ): (Trigger, TriggerMonitoringPriority, Int) = {
    val newTrigger: Trigger = TriggerBuilder.newTrigger()
      .withIdentity(name, group)
      .withDescription(description)
      .withSchedule(
      triggerType match {
        case "cron" => cron.get
        case "simple" => simple.get
      })
      .forJob(jobName, jobGroup)
      .usingJobData(jobDataMap.getOrElse(new JobDataMap()))
      .build()
    (newTrigger, TriggerMonitoringPriority.withName(triggerMonitoringPriority), triggerMaxErrorTime)
  }

  private def triggerFormUnapply(tp: (Trigger, TriggerMonitoringPriority, Int)):
    Option[
      (
        String,
        String,
        String,
        String,
        String,
        String,
        Option[SimpleScheduleBuilder],
        Option[CronScheduleBuilder],
        Option[JobDataMap], String, Int
      )
    ] = {
    val trigger = tp._1
    val (triggerType: String, simple, cron) = trigger match {
      case cron: CronTrigger => ("cron", None, Some(cron.getScheduleBuilder))
      case simple: SimpleTrigger => ("simple", Some(simple.getScheduleBuilder), None)
    }
    val description = if (trigger.getDescription() == null) "" else trigger.getDescription()
    Some(
      (
        triggerType,
        trigger.getKey.getGroup(),
        trigger.getKey.getName(),
        trigger.getJobKey.getGroup(),
        trigger.getJobKey.getName(),
        description,
        simple.asInstanceOf[Option[SimpleScheduleBuilder]],
        cron.asInstanceOf[Option[CronScheduleBuilder]],
        Some(trigger.getJobDataMap),
        tp._2.toString,
        tp._3
      )
    )
  }

  private def getCronParseError(cronExpression: String): String = {
    try {
      new CronExpression(cronExpression).getCronExpression()
    } catch {
      case e: ParseException => e.getMessage()
    }
  }

  def isValidCronExpression(cronExpression: String): Boolean = {
    try {
      new CronExpression(cronExpression)
      true
    } catch {
      case e: ParseException => false
    }
  }

  def validCronExpression: Constraint[String] = Constraint[String]("Invalid cron expression") { cronExpression =>
    if (!isValidCronExpression(cronExpression)) {
      Invalid(ValidationError(getCronParseError(cronExpression)))
    } else {
      Valid
    }
  }

  def buildTriggerForm = Form[(Trigger, TriggerMonitoringPriority, Int)](
    mapping(
      "triggerType" -> nonEmptyText(),
      "group" -> nonEmptyText(),
      "name" -> nonEmptyText(),
      "jobGroup" -> nonEmptyText(),
      "jobName" -> nonEmptyText(),
      "description" -> text(),
      "simple" -> optional(mapping(
        "repeatCount" -> number(),
        "repeatInterval" -> number()
      )(simpleScheduleFormApply)(simpleScheduleFormUnapply)),
      "cron" -> optional(mapping(
        "cronExpression" -> nonEmptyText().verifying(validCronExpression)
      )(cronScheduleFormApply)(cronScheduleFormUnapply)),
      "job-data-map" -> jobDataMap,
      "triggerMonitoringPriority" -> nonEmptyText(),
      "triggerMaxErrorTime" -> number()
    )(triggerFormApply)(triggerFormUnapply).verifying("Job does not exist", fields => {
      scheduler.checkExists(fields._1.getJobKey)
    }).verifying("Max time between successes must be greater than 0", fields => {
      fields._3 > 0
    })
  )
}
