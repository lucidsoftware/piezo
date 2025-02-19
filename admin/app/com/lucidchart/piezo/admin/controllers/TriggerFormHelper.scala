package com.lucidchart.piezo.admin.controllers

import com.lucidchart.piezo.TriggerMonitoringPriority
import com.lucidchart.piezo.TriggerMonitoringPriority.TriggerMonitoringPriority
import com.lucidchart.piezo.admin.models.MonitoringTeams
import com.lucidchart.piezo.admin.utils.CronHelper
import java.text.ParseException
import org.quartz.*
import play.api.data.{Form, FormError}
import play.api.data.Forms.*
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Constraints, Invalid, Valid, ValidationError}

case class TriggerFormValue(
  trigger: Trigger,
  priority: TriggerMonitoringPriority,
  maxErrorTime: Int,
  monitoringTeam: Option[String],
)
class TriggerFormHelper(scheduler: Scheduler, monitoringTeams: MonitoringTeams) extends JobDataHelper {

  private def simpleScheduleFormApply(repeatCount: Int, repeatInterval: Int): SimpleScheduleBuilder = {
    SimpleScheduleBuilder
      .simpleSchedule()
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
    Some(cronTrigger.getCronExpression())
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
    triggerMaxErrorTime: Int,
    triggerMonitoringTeam: Option[String],
  ): TriggerFormValue = {
    val newTrigger: Trigger = TriggerBuilder
      .newTrigger()
      .withIdentity(name, group)
      .withDescription(description)
      .withSchedule((triggerType match {
        case "cron" => cron.get
        case "simple" => simple.get
      }): ScheduleBuilder[?])
      .forJob(jobName, jobGroup)
      .usingJobData(jobDataMap.getOrElse(new JobDataMap()))
      .build()
    TriggerFormValue(
      newTrigger,
      TriggerMonitoringPriority.withName(triggerMonitoringPriority),
      triggerMaxErrorTime,
      triggerMonitoringTeam,
    )
  }

  private def triggerFormUnapply(value: TriggerFormValue): Option[
    (
      String,
      String,
      String,
      String,
      String,
      String,
      Option[SimpleScheduleBuilder],
      Option[CronScheduleBuilder],
      Option[JobDataMap],
      String,
      Int,
      Option[String],
    ),
  ] = {
    val trigger = value.trigger
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
        value.priority.toString,
        value.maxErrorTime,
        value.monitoringTeam,
      ),
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

  def buildTriggerForm: Form[TriggerFormValue] = Form(
    mapping(
      "triggerType" -> nonEmptyText(),
      "group" -> nonEmptyText(),
      "name" -> nonEmptyText(),
      "jobGroup" -> nonEmptyText(),
      "jobName" -> nonEmptyText(),
      "description" -> text(),
      "simple" -> optional(
        mapping(
          "repeatCount" -> number(),
          "repeatInterval" -> number(),
        )(simpleScheduleFormApply)(simpleScheduleFormUnapply),
      ),
      "cron" -> optional(
        mapping(
          "cronExpression" -> nonEmptyText().verifying(validCronExpression),
        )(cronScheduleFormApply)(cronScheduleFormUnapply),
      ),
      "job-data-map" -> jobDataMap,
      "triggerMonitoringPriority" -> nonEmptyText(),
      "triggerMaxErrorTime" -> of(MaxSecondsBetweenSuccessesFormatter).verifying(Constraints.min(0)),
      "triggerMonitoringTeam" -> optional(text()),
    )(triggerFormApply)(triggerFormUnapply)
      .verifying(
        "Job does not exist",
        fields => {
          scheduler.checkExists(fields.trigger.getJobKey)
        },
      )
      .verifying(
        "A valid team is required if monitoring is on",
        fields => {
          !monitoringTeams.teamsDefined || fields.priority == TriggerMonitoringPriority.Off || fields.monitoringTeam
            .exists(monitoringTeams.value.contains[String])
        },
      ),
  )
}

object MaxSecondsBetweenSuccessesFormatter extends Formatter[Int] {
  override val format: Option[(String, Seq[Nothing])] = Some(("format.triggerMaxErrorTime", Nil))
  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] = {
    for {
      maxSecondsBetweenSuccesses <- parsing(_.toInt, "Numeric value expected", Nil)(key, data)
      maxIntervalTime <- {
        if (data.contains("cron.cronExpression")) {
          parsing(expr => CronHelper.getMaxInterval(expr), "try again.", Nil)(
            "cron.cronExpression",
            data,
          )
        } else {
          parsing(_.toLong, "try again.", Nil)("simple.repeatInterval", data)
        }
      }
      _ <- Either.cond(
        maxSecondsBetweenSuccesses > maxIntervalTime,
        maxSecondsBetweenSuccesses,
        List(
          FormError(
            "triggerMaxErrorTime",
            s"Must be greater than the maximum trigger interval ($maxIntervalTime seconds)",
          ),
        ),
      )
    } yield maxSecondsBetweenSuccesses
  }
  override def unbind(key: String, value: Int): Map[String, String] = Map(key -> value.toString)
}
