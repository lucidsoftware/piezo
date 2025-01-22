package com.lucidchart.piezo.admin.controllers

import com.lucidchart.piezo.{TriggerHistoryModel, TriggerMonitoringModel, TriggerMonitoringPriority, WorkerSchedulerFactory}
import java.util.Date
import org.quartz.Trigger.TriggerState
import org.quartz._
import org.quartz.impl.triggers.{CronTriggerImpl, SimpleTriggerImpl}
import play.api._
import play.api.libs.json._
import play.api.mvc._
import com.lucidchart.piezo.admin.models._
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try
import play.api.Logging
import play.api.i18n.I18nSupport

class Triggers(schedulerFactory: WorkerSchedulerFactory, cc: ControllerComponents, monitoringTeams: MonitoringTeams)
  extends AbstractController(cc) with Logging with ErrorLogging with play.api.i18n.I18nSupport {

  val scheduler = logExceptions(schedulerFactory.getScheduler())
  val properties = schedulerFactory.props
  val triggerHistoryModel = logExceptions(new TriggerHistoryModel(properties))
  val triggerMonitoringPriorityModel = logExceptions(new TriggerMonitoringModel(properties))
  val triggerFormHelper = new TriggerFormHelper(scheduler, monitoringTeams)

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
     Ok(
       com.lucidchart.piezo.admin.views.html.triggers(
        TriggerHelper.getTriggersByGroup(scheduler),
        None,
        allTriggers,
        scheduler.getMetaData
       )(request)
     )
   }

  def getTrigger(group: String, name: String) = Action { implicit request =>
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      val errorMsg = Some("Trigger " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, errorMsg)(request))
    } else {
      try {
        val triggerDetail = scheduler.getTrigger(triggerKey)

        val history = {
          try {
            Some(triggerHistoryModel.getTrigger(triggerKey))
          } catch {
            case e: Exception => {
              logger.error("Failed to get trigger history")
              None
            }
          }
        }

        val (triggerMonitoringPriority, triggerMaxErrorTime, triggerMonitoringTeam) = Try {
          triggerMonitoringPriorityModel.getTriggerMonitoringRecord(
            triggerDetail.getKey
          ).map { triggerMonitoringRecord =>
            (triggerMonitoringRecord.priority, triggerMonitoringRecord.maxSecondsInError, triggerMonitoringRecord.monitoringTeam)
          }.getOrElse((TriggerMonitoringPriority.Low, 300, None))
        }.getOrElse {
          logger.error("Failed to get trigger monitoring info")
          (TriggerMonitoringPriority.Low, 300, None)
        }

        Ok(
          com.lucidchart.piezo.admin.views.html.trigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            Some(triggerDetail),
            history,
            None,
            Some(triggerMonitoringPriority),
            triggerMaxErrorTime,
            triggerMonitoringTeam,
            triggerState = Some(scheduler.getTriggerState(triggerKey))
          )(request)
        )
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting trigger " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(
            com.lucidchart.piezo.admin.views.html.trigger(
              mutable.Buffer(),
              None,
              None,
              Some(errorMsg)
            )(request)
          )
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
        triggerMonitoringPriorityModel.deleteTriggerMonitoringRecord(triggerKey)
        Ok(
          com.lucidchart.piezo.admin.views.html.trigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            None,
            None
          )(request)
        )
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught deleting trigger " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(
            com.lucidchart.piezo.admin.views.html.trigger(
              mutable.Buffer(),
              None,
              None,
              Some(errorMsg)
            )(request)
          )
        }
      }
    }
  }

  val formNewAction = routes.Triggers.postTrigger()
  def formEditAction(group: String, name: String): Call = routes.Triggers.putTrigger(group, name)

  def getNewTriggerForm(
    triggerType: String = "cron",
    jobGroup: String = "",
    jobName: String = "",
    templateGroup: Option[String] = None,
    templateName: Option[String] = None
  ) = Action { implicit request =>
    templateGroup match {
      case Some(group) => getEditTrigger(group, templateName.get, true)
      case None =>
        val dummyTrigger = triggerType match {
          case "cron" => new DummyCronTrigger(jobGroup, jobName)
          case "simple" => new DummySimpleTrigger(jobGroup, jobName)
        }
        val newTriggerForm = triggerFormHelper.buildTriggerForm.fill(
          TriggerFormValue(dummyTrigger, TriggerMonitoringPriority.Low, 300, None)
        )
        Ok(
          com.lucidchart.piezo.admin.views.html.editTrigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            monitoringTeams.value,
            newTriggerForm,
            formNewAction,
            false,
            false
          )
        )
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
      val (triggerMonitoringPriority, triggerMaxErrorTime, triggerMonitoringTeam) = Try {
        triggerMonitoringPriorityModel.getTriggerMonitoringRecord(
          triggerDetail.getKey,
        ).map { triggerMonitoringRecord =>
          (triggerMonitoringRecord.priority, triggerMonitoringRecord.maxSecondsInError, triggerMonitoringRecord.monitoringTeam)
        }.getOrElse((TriggerMonitoringPriority.Low, 300, None))
      }.getOrElse {
        logger.error("Failed to get trigger monitoring info")
        (TriggerMonitoringPriority.Low, 300, None)
      }
      val editTriggerForm = triggerFormHelper.buildTriggerForm.fill(
        TriggerFormValue(triggerDetail, triggerMonitoringPriority, triggerMaxErrorTime, triggerMonitoringTeam)
      )
      if (isTemplate) {
        Ok(
          com.lucidchart.piezo.admin.views.html.editTrigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            monitoringTeams.value,
            editTriggerForm,
            formNewAction,
            false,
            isTemplate
          )
        )
      }
      else {
        Ok(
          com.lucidchart.piezo.admin.views.html.editTrigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            monitoringTeams.value,
            editTriggerForm,
            formEditAction(group, name),
            true,
            isTemplate
          )
        )
      }
    }
  }

  def getEditTriggerAction(group: String, name: String) = Action { implicit request =>
    getEditTrigger(group, name, false)
  }

  def putTrigger(group: String, name: String) = Action { implicit request =>
    triggerFormHelper.buildTriggerForm.bindFromRequest().fold(
      formWithErrors =>
        BadRequest(
          com.lucidchart.piezo.admin.views.html.editTrigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            monitoringTeams.value,
            formWithErrors,
            formEditAction(group, name),
            true,
            false
          )
        ),
      value => {
        val TriggerFormValue(trigger, triggerMonitoringPriority, triggerMaxErrorTime, triggerMonitoringTeam) = value
        scheduler.rescheduleJob(trigger.getKey(), trigger)
        triggerMonitoringPriorityModel.setTriggerMonitoringRecord(
          trigger.getKey,
          triggerMonitoringPriority,
          triggerMaxErrorTime,
          triggerMonitoringTeam
        )
        Redirect(routes.Triggers.getTrigger(trigger.getKey.getGroup(), trigger.getKey.getName()))
          .flashing("message" -> "Successfully added trigger.", "class" -> "")
      }
    )
  }

  def postTrigger() = Action { implicit request =>
    triggerFormHelper.buildTriggerForm.bindFromRequest().fold(
      formWithErrors =>
        BadRequest(
          com.lucidchart.piezo.admin.views.html.editTrigger(
            TriggerHelper.getTriggersByGroup(scheduler),
            monitoringTeams.value,
            formWithErrors,
            formNewAction,
            false,
            false
          )
        ),
      value => {
        val TriggerFormValue(trigger, triggerMonitoringPriority, triggerMaxErrorTime, triggerMonitoringTeam) = value
        try {
          scheduler.scheduleJob(trigger)
          triggerMonitoringPriorityModel.setTriggerMonitoringRecord(
            trigger.getKey,
            triggerMonitoringPriority,
            triggerMaxErrorTime,
            triggerMonitoringTeam
          )
          Redirect(routes.Triggers.getTrigger(trigger.getKey.getGroup(), trigger.getKey.getName()))
            .flashing("message" -> "Successfully added trigger.", "class" -> "")
        } catch {
          case alreadyExists: ObjectAlreadyExistsException =>
            val form = triggerFormHelper.buildTriggerForm.fill(
              TriggerFormValue(trigger, triggerMonitoringPriority, triggerMaxErrorTime, triggerMonitoringTeam)
            )
            Ok(
              com.lucidchart.piezo.admin.views.html.editTrigger(
                TriggerHelper.getTriggersByGroup(scheduler),
                monitoringTeams.value,
                form,
                formNewAction,
                false,
                false,
                errorMessage = Some("Please provide unique group-name pair")
              )
            )
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

  def patchTrigger(group: String, name: String) = Action { implicit request =>
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(
        mutable.Buffer(),
        None,
        None,
        errorMessage = Some(s"Trigger $group:$name not found"))(request)
      )
    } else {
      request.body.asJson.map { json =>
        (json \ "state").asOpt[String].map { state =>
          if(state.equalsIgnoreCase(TriggerState.PAUSED.toString)) {
            scheduler.pauseTrigger(triggerKey)
          } else {
            val currentState = scheduler.getTriggerState(triggerKey).toString
            if(currentState == TriggerState.ERROR.toString) {
              val trigger = scheduler.getTrigger(triggerKey)
              scheduler.rescheduleJob(triggerKey, trigger)
            } else {
              scheduler.resumeTrigger(triggerKey)
            }
          }
          Ok(Json.obj("state" -> scheduler.getTriggerState(triggerKey).toString))
        }.getOrElse {
          BadRequest("Missing parameter [state]")
        }
      }.getOrElse {
        BadRequest("Expecting Json data")
      }
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
