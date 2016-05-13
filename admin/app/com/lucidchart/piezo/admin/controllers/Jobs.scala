package com.lucidchart.piezo.admin.controllers

import com.lucidchart.piezo.admin.utils.JobUtils
import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{JobHistoryModel, WorkerSchedulerFactory}
import org.quartz.impl.matchers.GroupMatcher
import scala.collection.JavaConverters._
import scala.collection.mutable
import org.quartz._
import scala.Some
import scala.collection.JavaConverters._
import com.lucidchart.piezo.admin.views._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Jobs extends Jobs(new WorkerSchedulerFactory())

class Jobs(schedulerFactory: WorkerSchedulerFactory) extends Controller {
  implicit val logger = Logger(this.getClass())

  val scheduler = logExceptions(schedulerFactory.getScheduler())
  val properties = schedulerFactory.props
  val jobHistoryModel = logExceptions(new JobHistoryModel(properties))
  val jobFormHelper = new JobFormHelper()

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
    val allJobs: List[JobKey] = getJobsByGroup().foldLeft(List[JobKey]())({(finalList, jobsForGroup) =>
      finalList ::: jobsForGroup._2})
    val jobHistories = allJobs.flatMap({ job =>
      jobHistoryModel.getJob(job.getName, job.getGroup).headOption
    })
    Ok(com.lucidchart.piezo.admin.views.html.jobs(getJobsByGroup(), None, Some(jobHistories), scheduler.getMetaData)(request))
  }

  def getJob(group: String, name: String) = Action { implicit request =>
    val jobKey = new JobKey(name, group)
    val jobExists = scheduler.checkExists(jobKey)
    if (!jobExists) {
      val errorMsg = Some("Job " + group + " " + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), None, None, None, errorMsg)(request))
    } else {
      try {
        val jobDetail: Option[JobDetail] = Some(scheduler.getJobDetail(jobKey))

        val history = {
          try {
            Some(jobHistoryModel.getJob(name, group))
          } catch {
            case e: Exception => {
              logger.error("Failed to get job history")
              None
            }
          }
        }

        val triggers = Some(scheduler.getTriggersOfJob(jobKey).asScala.toList)

        Ok(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), jobDetail, history, triggers)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting job " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), None, None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  def deleteJob(group: String, name: String) = Action { implicit request =>
    val jobKey = new JobKey(name, group)
    if (!scheduler.checkExists(jobKey)) {
      val errorMsg = Some("Job %s $s not found".format(group, name))
      NotFound(com.lucidchart.piezo.admin.views.html.job(mutable.Buffer(), None, None, None, errorMsg)(request))
    } else {
      try {
        scheduler.deleteJob(jobKey)
        Ok(com.lucidchart.piezo.admin.views.html.job(getJobsByGroup(), None, None, None)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught deleting job %s %s. -- %s".format(group, name, e.getLocalizedMessage())
          logger.error(errorMsg, e)
          InternalServerError(com.lucidchart.piezo.admin.views.html.job(mutable.Buffer(), None, None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  val submitNewMessage = "Create"
  val formNewAction = routes.Jobs.postJob()
  val submitEditMessage = "Save"
  def formEditAction(group: String, name: String): Call = routes.Jobs.putJob(group, name)

  def getNewJobForm() = Action { implicit request =>
    val newJobForm = jobFormHelper.buildJobForm
    Ok(com.lucidchart.piezo.admin.views.html.editJob(getJobsByGroup(), newJobForm, submitNewMessage, formNewAction, false)(request))
  }

  def getEditJob(group: String, name: String) = Action { implicit request =>
    val jobKey = new JobKey(name, group)

    if (scheduler.checkExists(jobKey)) {
      val jobDetail = scheduler.getJobDetail(jobKey)
      val editJobForm = jobFormHelper.buildJobForm().fill(jobDetail)
      Ok(com.lucidchart.piezo.admin.views.html.editJob(getJobsByGroup(), editJobForm, submitEditMessage, formEditAction(group, name), true)(request))
    } else {
      val errorMsg = Some("Job %s %s not found".format(group, name))
      NotFound(com.lucidchart.piezo.admin.views.html.job(mutable.Buffer(), None, None, None, errorMsg)(request))
    }
  }

  def putJob(group: String, name: String) = Action { implicit request =>
    jobFormHelper.buildJobForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(html.editJob(getJobsByGroup(), formWithErrors, submitNewMessage, formNewAction, false)),
      value => {
        val jobDetail = JobUtils.cleanup(value)
        scheduler.addJob(jobDetail, true)
        Redirect(routes.Jobs.getJob(value.getKey.getGroup(), value.getKey.getName()))
          .flashing("message" -> "Successfully edited job.", "class" -> "")
      }
    )
  }

  def postJob() = Action { implicit request =>
    jobFormHelper.buildJobForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(com.lucidchart.piezo.admin.views.html.editJob(getJobsByGroup(), formWithErrors, submitNewMessage, formNewAction, false)),
      value => {
        val jobDetail = JobUtils.cleanup(value)
        scheduler.addJob(jobDetail, false)
        Redirect(routes.Jobs.getJob(value.getKey.getGroup(), value.getKey.getName()))
          .flashing("message" -> "Successfully added job.", "class" -> "")
      }
    )
  }

  def jobGroupTypeAhead(sofar: String) = Action { implicit request =>
    val groups = scheduler.getJobGroupNames().asScala.toList

    Ok(Json.obj("groups" -> groups.filter{ group =>
      group.toLowerCase.contains(sofar.toLowerCase)
    }))
  }

  def jobNameTypeAhead(group: String, sofar: String) = Action { implicit request =>
    val jobs = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).asScala.toSet

    Ok(Json.obj("jobs" -> jobs.filter(_.getName.toLowerCase.contains(sofar.toLowerCase)).map(_.getName)))
  }


}
