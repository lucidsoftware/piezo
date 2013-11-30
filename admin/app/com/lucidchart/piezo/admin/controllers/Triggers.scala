package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{TriggerHistoryModel, WorkerSchedulerFactory}
import org.quartz._
import impl.matchers.GroupMatcher
import scala.collection.JavaConverters._
import scala.collection.mutable

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
     Ok(com.lucidchart.piezo.admin.views.html.triggers(getTriggersByGroup(), None)(request))
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
 }