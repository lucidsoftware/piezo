package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{TriggerHistoryModel, WorkerSchedulerFactory}
import org.quartz._
import impl.matchers.GroupMatcher

import scala.collection.JavaConverters._
import scala.collection.mutable
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import java.util.{Date, TimeZone}
import org.quartz.impl.triggers.{CronTriggerImpl, SimpleTriggerImpl}

import play.api.data.format.Formatter
import java.text.ParseException

import org.quartz.Trigger.TriggerTimeComparator
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.libs.json._

object Triggers extends Triggers(new WorkerSchedulerFactory())

class Triggers(schedulerFactory: WorkerSchedulerFactory) extends Controller {
  implicit val logger = Logger(this.getClass())

  val scheduler = logExceptions(schedulerFactory.getScheduler())
  val properties = schedulerFactory.props
  val triggerHistoryModel = logExceptions(new TriggerHistoryModel(properties))
  val triggerFormHelper = new TriggerFormHelper(scheduler)

  def firesFirst(time: Date)(trigger1: Trigger, trigger2: Trigger): Boolean = {
    val time1 = trigger1.getFireTimeAfter(time)
    val time2 = trigger2.getFireTimeAfter(time)
    if (time2 == null) true
    else if (time1 == null) false
    else if (time1 != time2) time1 before time2
    else trigger1.getPriority > trigger2.getPriority
  }

   def getIndex = Action { implicit request =>
     val now = new Date()
     val allTriggers: List[Trigger] = TriggerHelper.getTriggersByGroup(scheduler).flatMap { case (group, triggerKeys) =>
       triggerKeys.map(triggerKey => scheduler.getTrigger(triggerKey))
     }.toList.sortWith(firesFirst(now))
     Ok(com.lucidchart.piezo.admin.views.html.triggers(TriggerHelper.getTriggersByGroup(scheduler), None, allTriggers, scheduler.getMetaData)(request))
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

        Ok(com.lucidchart.piezo.admin.views.html.trigger(TriggerHelper.getTriggersByGroup(scheduler), triggerDetail, history)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting trigger " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  def deleteTrigger(group: String, name: String) = Action { implicit request =>
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      val errorMsg = Some("Trigger " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, errorMsg)(request))
    } else {
      try {
        scheduler.unscheduleJob(triggerKey)
        Ok(com.lucidchart.piezo.admin.views.html.trigger(TriggerHelper.getTriggersByGroup(scheduler), None, None)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught deleting trigger " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  val formNewAction = routes.Triggers.postTrigger()
  def formEditAction(group: String, name: String): Call = routes.Triggers.putTrigger(group, name)

  def getNewTriggerForm(triggerType: String = "cron", jobGroup: String = "", jobName: String = "", templateGroup: Option[String] = None, templateName: Option[String] = None) = Action { implicit request =>
    templateGroup match {
      case Some(group) => getEditTrigger(group, templateName.get, true)
      case None =>
        val dummyTrigger = triggerType match {
          case "cron" => new DummyCronTrigger(jobGroup, jobName)
          case "simple" => new DummySimpleTrigger(jobGroup, jobName)
        }
        val newTriggerForm = triggerFormHelper.buildTriggerForm().fill(dummyTrigger)
        Ok(com.lucidchart.piezo.admin.views.html.editTrigger(TriggerHelper.getTriggersByGroup(scheduler), newTriggerForm, formNewAction, false, false)(request))
    }
  }

  def getEditTrigger(group: String, name: String, isTemplate: Boolean)(implicit request: Request[AnyContent]) = {
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      val errorMsg = Some("Trigger " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, errorMsg)(request))
    } else {
      val triggerDetail: Trigger = scheduler.getTrigger(triggerKey)
      val editTriggerForm = triggerFormHelper.buildTriggerForm().fill(triggerDetail)
      if (isTemplate) Ok(com.lucidchart.piezo.admin.views.html.editTrigger(TriggerHelper.getTriggersByGroup(scheduler), editTriggerForm, formNewAction, false, isTemplate)(request))
      else Ok(com.lucidchart.piezo.admin.views.html.editTrigger(TriggerHelper.getTriggersByGroup(scheduler), editTriggerForm, formEditAction(group, name), true, isTemplate)(request))
    }
  }

  def getEditTriggerAction(group: String, name: String) = Action { implicit request => getEditTrigger(group, name, false) }

  def putTrigger(group: String, name: String) = Action { implicit request =>
    triggerFormHelper.buildTriggerForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(com.lucidchart.piezo.admin.views.html.editTrigger(TriggerHelper.getTriggersByGroup(scheduler), formWithErrors, formEditAction(group, name), true, false)),
      value => {
        scheduler.rescheduleJob(value.getKey(), value)
        Redirect(routes.Triggers.getTrigger(value.getKey.getGroup(), value.getKey.getName()))
          .flashing("message" -> "Successfully added trigger.", "class" -> "")
      }
    )
  }

  def postTrigger() = Action { implicit request =>
    triggerFormHelper.buildTriggerForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(com.lucidchart.piezo.admin.views.html.editTrigger(TriggerHelper.getTriggersByGroup(scheduler), formWithErrors, formNewAction, false, false)),
      value => {
        try {
          scheduler.scheduleJob(value)
          Redirect(routes.Triggers.getTrigger(value.getKey.getGroup(), value.getKey.getName()))
            .flashing("message" -> "Successfully added trigger.", "class" -> "")
        } catch {
          case alreadyExists: ObjectAlreadyExistsException =>
            val form = triggerFormHelper.buildTriggerForm.fill(value)
            Ok(com.lucidchart.piezo.admin.views.html.editTrigger(TriggerHelper.getTriggersByGroup(scheduler), form, formNewAction, false, false, errorMessage = Some("Please provide unique group-name pair"))(request))
        }
      }
    )
  }

  def triggerGroupTypeAhead(sofar: String) = Action { implicit request =>
    val groups = scheduler.getTriggerGroupNames().asScala.toList

    Ok(Json.obj("groups" -> groups.filter{ group =>
      group.toLowerCase.contains(sofar.toLowerCase)
    }))
  }

  def triggerJob(group: String, name: String) = Action { implicit request =>
    val jobKey = new JobKey(name, group)

    if (scheduler.checkExists(jobKey)) {
      try {
        scheduler.triggerJob(jobKey)
        Ok
      } catch {
        case e: SchedulerException => {
          logger.error("Exception caught triggering job %s %s. -- %s".format(group, name, e.getLocalizedMessage), e)
          InternalServerError
        }
      }
    } else {
      NotFound
    }
  }

  private trait DummyTrigger extends Trigger {
    override def getKey() = { new TriggerKey("", "") }

    override def getJobKey() = { new JobKey("", "") }

    override def getDescription() = ""
  }

  private class DummyCronTrigger(jobGroup: String, jobName: String) extends CronTriggerImpl with DummyTrigger {
    override def getCronExpression() = "7 7 7 * * ?"
    override def getJobKey() = { new JobKey(jobName, jobGroup)}
  }

  private class DummySimpleTrigger(jobGroup: String, jobName: String) extends SimpleTriggerImpl with DummyTrigger {
    override def getJobKey() = { new JobKey(jobName, jobGroup)}
  }
}