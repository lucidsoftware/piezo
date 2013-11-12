package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{WorkerSchedulerFactory}
import org.quartz.impl.matchers.GroupMatcher
import scala.collection.JavaConverters._
import scala.collection.mutable
import org.quartz.{JobDetail, JobKey}

object Jobs extends Controller {
  val logger = Logger(this.getClass())
  private def logExceptions[T](value: => T): T =
  {
    try {
      value
    }
    catch {
      case t: Throwable =>
        logger.error("Caught exception initializing class", t)
        throw t
    }
  }

  val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
  val scheduler = logExceptions(schedulerFactory.getScheduler())
  val props = schedulerFactory.props

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

  def getJob(group: String, name: String) = Action { implicit request =>
    val jobKey = new JobKey(name, group)
    val jobExists = scheduler.checkExists(jobKey)
    if (!jobExists) {
      val errorMsg = Some("Job " + group + "/" + name + " not found")
      NotFound(com.lucidchart.piezo.admin.views.html.jobs(getJobsByGroup(), None, errorMsg)(request))
    } else {
      val jobDetail: Option[JobDetail] = Some(scheduler.getJobDetail(jobKey))
      Ok(com.lucidchart.piezo.admin.views.html.jobs(getJobsByGroup(), jobDetail)(request))
    }
  }
 }
