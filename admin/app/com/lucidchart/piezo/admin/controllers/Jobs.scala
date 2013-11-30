package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{JobHistoryModel, WorkerSchedulerFactory}
import org.quartz.impl.matchers.GroupMatcher
import scala.collection.JavaConverters._
import scala.collection.mutable
import org.quartz._
import scala.Some
import java.io.{PrintWriter, StringWriter}

object Jobs extends Controller {
  implicit val logger = Logger(this.getClass())

  val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
  val scheduler = logExceptions(schedulerFactory.getScheduler())

  def getJobsByGroup(): mutable.Buffer[(String, List[JobKey])] = {
    val jobsByGroup =
      for (groupName <- scheduler.getJobGroupNames().asScala) yield {
        val jobs: List[JobKey] = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).asScala.toList
        val sortedJobs: List[JobKey] = jobs.sortBy(jobKey => jobKey.getName())
        (groupName, sortedJobs)
      }
    jobsByGroup.sortBy(groupList => groupList._1)
  }

  def getIndex = Action { implicit request =>
    Ok(com.lucidchart.piezo.admin.views.html.jobs(getJobsByGroup(), None)(request))
  }

  private[controllers] def getRealOrDummyJob(jobKey: JobKey): Option[JobDetail] = {
    try {
      Some(scheduler.getJobDetail(jobKey))
    }
    catch {
      case e: JobPersistenceException => {
        e.getCause() match {
          case e: ClassNotFoundException => {
            Some(scheduler.getJobDetail(jobKey))
          }
        }
      }
    }
  }

  def getJob(group: String, name: String) = Action { implicit request =>
    val jobKey = new JobKey(name, group)
    val jobExists = scheduler.checkExists(jobKey)
    if (!jobExists) {
      val errorMsg = Some("Job " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), None, None, errorMsg)(request))
    } else {
      try {
        val jobDetail: Option[JobDetail] = getRealOrDummyJob(jobKey)

        val history = {
          try {
            val jobHistoryModel = new JobHistoryModel(schedulerFactory.props)
            Some(jobHistoryModel.getJob(name, group))
          } catch {
            case e:Exception => {
              logger.error("Failed to get job history")
              None
            }
          }
        }

        Ok(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), jobDetail, history)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting job " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), None, None, Some(errorMsg))(request))
        }
      }
    }
  }
 }
