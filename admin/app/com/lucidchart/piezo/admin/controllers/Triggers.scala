package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{TriggerHistoryModel, WorkerSchedulerFactory}
import org.quartz._
import impl.matchers.GroupMatcher
import scala.collection.JavaConverters._
import scala.collection.mutable
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import java.util.{TimeZone, Date}
import org.quartz.impl.triggers.{SimpleTriggerImpl, CronTriggerImpl}

object Triggers extends Controller {
  implicit val logger = Logger(this.getClass())

  val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
  val scheduler = logExceptions(schedulerFactory.getScheduler())
  val properties = schedulerFactory.props
  val triggerHistoryModel = logExceptions(new TriggerHistoryModel(properties))

  def getTriggersByGroup(): mutable.Buffer[(String, List[TriggerKey])] = {
    val triggersByGroup =
      for (groupName <- scheduler.getTriggerGroupNames().asScala) yield {
        val triggers: List[TriggerKey] = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName)).asScala.toList
        val sortedTriggers: List[TriggerKey] = triggers.sortBy(triggerKey => triggerKey.getName())
        (groupName, sortedTriggers)
      }
    triggersByGroup.sortBy(groupList => groupList._1)
  }

   def getIndex = Action { implicit request =>
     Ok(com.lucidchart.piezo.admin.views.html.triggers(getTriggersByGroup(), None, scheduler.getMetaData)(request))
   }

  def getTrigger(group: String, name: String) = Action { implicit request =>
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      val errorMsg = Some("Trigger " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, errorMsg)(request))
    } else {
      try {
        val triggerDetail: Option[Trigger] = Some(scheduler.getTrigger(triggerKey))

        val history = {
          try {
            Some(triggerHistoryModel.getTrigger(name, group))
          } catch {
            case e: Exception => {
              logger.error("Failed to get trigger history")
              None
            }
          }
        }

        Ok(com.lucidchart.piezo.admin.views.html.trigger(getTriggersByGroup(), triggerDetail, history)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting trigger " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  private def simpleScheduleFormApply(repeatCount: Int, repeatInterval: Int): SimpleScheduleBuilder = {
    SimpleScheduleBuilder.simpleSchedule()
    .withRepeatCount(repeatCount)
    .withIntervalInSeconds(repeatInterval)
  }

  private def simpleScheduleFormUnapply(simple: SimpleScheduleBuilder) = {
    val simpleTrigger = simple.build().asInstanceOf[SimpleTrigger]
    Some((simpleTrigger.getRepeatCount, simpleTrigger.getRepeatInterval.toInt))
  }

  private def cronScheduleFormApply(cronExpression: String): CronScheduleBuilder = {
    CronScheduleBuilder.cronSchedule(cronExpression)
  }

  private def cronScheduleFormUnapply(cron: CronScheduleBuilder) = {
    val cronTrigger = cron.build().asInstanceOf[CronTrigger]
    Some((cronTrigger.getCronExpression()))
  }

  private def triggerFormApply(triggerType: String, group: String, name: String, jobGroup: String, jobName: String, description: String,
                               simple: Option[SimpleScheduleBuilder], cron: Option[CronScheduleBuilder]): Trigger = {
    val newTrigger: Trigger = TriggerBuilder.newTrigger()
      .withIdentity(name, group)
      .withDescription(description)
      .withSchedule(
      triggerType match {
        case "cron" => cron.get
        case "simple" => simple.get
      })
      .forJob(jobName, jobGroup)
      .build()
    newTrigger
  }

  private def triggerFormUnapply(trigger: Trigger):
  Option[(String, String, String, String, String, String, Option[SimpleScheduleBuilder], Option[CronScheduleBuilder])] = {
    val (triggerType: String, simple, cron) = trigger match {
      case cron: CronTrigger => ("cron", None, Some(cron.getScheduleBuilder))
      case simple: SimpleTrigger => ("simple", Some(simple.getScheduleBuilder), None)
    }
    Some((triggerType, trigger.getKey.getGroup(), trigger.getKey.getName(),
      trigger.getJobKey.getGroup(), trigger.getJobKey.getName(), trigger.getDescription(),
      simple.asInstanceOf[Option[SimpleScheduleBuilder]], cron.asInstanceOf[Option[CronScheduleBuilder]]))
  }

  private def buildTriggerForm() = Form[Trigger](
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
        "cronExpression" -> nonEmptyText()
      )(cronScheduleFormApply)(cronScheduleFormUnapply))
    )(triggerFormApply)(triggerFormUnapply)
  )

  val submitNewMessage = "Create"
  val formNewAction = routes.Triggers.postTrigger()
  val submitEditMessage = "Save"
  def formEditAction(group: String, name: String): Call = routes.Triggers.putTrigger(group, name)

  def getNewTriggerForm(triggerType: String = "cron") = Action { implicit request =>
    val dummyTrigger = triggerType match {
      case "cron" => new DummyCronTrigger()
      case "simple" => new DummySimpleTrigger()
    }
    val newTriggerForm = buildTriggerForm().fill(dummyTrigger)
    Ok(com.lucidchart.piezo.admin.views.html.editTrigger(getTriggersByGroup(), newTriggerForm, submitNewMessage, formNewAction)(request))
  }

  def getEditTrigger(group: String, name: String) = Action { implicit request =>
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      val errorMsg = Some("Trigger " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, errorMsg)(request))
    } else {
      val triggerDetail: Trigger = scheduler.getTrigger(triggerKey)
      val editTriggerForm = buildTriggerForm().fill(triggerDetail)
      Ok(com.lucidchart.piezo.admin.views.html.editTrigger(getTriggersByGroup(), editTriggerForm, submitEditMessage, formEditAction(group, name))(request))
    }
  }

  def putTrigger(group: String, name: String) = Action { implicit request =>
    buildTriggerForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(com.lucidchart.piezo.admin.views.html.editTrigger(getTriggersByGroup(), formWithErrors, submitEditMessage, formEditAction(group, name))),
      value => {
        scheduler.rescheduleJob(value.getKey(), value)
        Redirect(routes.Triggers.getTrigger(value.getKey.getGroup(), value.getKey.getName()))
          .flashing("message" -> "Successfully added trigger.", "class" -> "")
      }
    )
  }

  def postTrigger() = Action { implicit request =>
    buildTriggerForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(com.lucidchart.piezo.admin.views.html.editTrigger(getTriggersByGroup(), formWithErrors, submitNewMessage, formNewAction)),
      value => {
        scheduler.scheduleJob(value)
        Redirect(routes.Triggers.getTrigger(value.getKey.getGroup(), value.getKey.getName()))
          .flashing("message" -> "Successfully added trigger.", "class" -> "")
      }
    )
  }

  private trait DummyTrigger extends Trigger {
    override def getKey() = { new TriggerKey("", "") }

    override def getJobKey() = { new JobKey("", "") }

    override def getDescription() = ""
  }

  private class DummyCronTrigger extends CronTriggerImpl with DummyTrigger {
    override def getCronExpression() = "7 7 7 * * ?"
  }

  private class DummySimpleTrigger extends SimpleTriggerImpl with DummyTrigger {}
}