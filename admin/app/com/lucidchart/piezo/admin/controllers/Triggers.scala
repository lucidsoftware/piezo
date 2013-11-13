package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.WorkerSchedulerFactory
import org.quartz.{TriggerKey, Trigger}
import scala.collection.mutable

object Triggers extends Controller {
  implicit val logger = Logger(this.getClass())

  val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
  val scheduler = logExceptions(schedulerFactory.getScheduler())

   def getIndex = Action { implicit request =>
     Ok(com.lucidchart.piezo.admin.views.html.triggers(mutable.Buffer(), None)(request))
   }

  def getTrigger(group: String, name: String) = Action { implicit request =>
    val triggerKey = new TriggerKey(name, group)
    val triggerExists = scheduler.checkExists(triggerKey)
    if (!triggerExists) {
      val errorMsg = Some("Trigger " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, errorMsg)(request))
    } else {
      try {
        val triggerDetail: Option[Trigger] = Some(scheduler.getTrigger(triggerKey))
        triggerDetail.get.getJobDataMap.getKeys
        Ok(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), triggerDetail)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting trigger " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, Some(errorMsg))(request))
        }
      }
    }
  }
 }