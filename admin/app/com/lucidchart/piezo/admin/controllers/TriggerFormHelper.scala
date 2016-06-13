package com.lucidchart.piezo.admin.controllers

import org.quartz._
import scala.Some
import scala.Some
import scala.Some
import java.text.ParseException
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import play.api.data.validation.ValidationError


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

  private def triggerFormApply(triggerType: String, group: String, name: String, jobGroup: String, jobName: String, description: String,
                               simple: Option[SimpleScheduleBuilder], cron: Option[CronScheduleBuilder], jobDataMap: Option[JobDataMap]): Trigger = {
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
    newTrigger
  }

  private def triggerFormUnapply(trigger: Trigger):
  Option[(String, String, String, String, String, String, Option[SimpleScheduleBuilder], Option[CronScheduleBuilder], Option[JobDataMap])] = {
    val (triggerType: String, simple, cron) = trigger match {
      case cron: CronTrigger => ("cron", None, Some(cron.getScheduleBuilder))
      case simple: SimpleTrigger => ("simple", Some(simple.getScheduleBuilder), None)
    }
    val description = if (trigger.getDescription() == null) "" else trigger.getDescription()
    Some((triggerType, trigger.getKey.getGroup(), trigger.getKey.getName(),
      trigger.getJobKey.getGroup(), trigger.getJobKey.getName(), description,
      simple.asInstanceOf[Option[SimpleScheduleBuilder]], cron.asInstanceOf[Option[CronScheduleBuilder]], Some(trigger.getJobDataMap)))
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

  def buildTriggerForm() = Form[Trigger](
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
      "job-data-map" -> jobDataMap
    )(triggerFormApply)(triggerFormUnapply) verifying("Job does not exist", trigger => {
      scheduler.checkExists(trigger.getJobKey)
    })
  )
}
