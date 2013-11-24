package com.lucidchart.piezo.admin.controllers

import play.api._
import play.api.mvc._
import com.lucidchart.piezo.{JobHistoryModel, WorkerSchedulerFactory}
import org.quartz.impl.matchers.GroupMatcher
import scala.collection.JavaConverters._
import scala.collection.mutable
import org.quartz._
import scala.Some
import com.lucidchart.piezo.admin.util.DummyClassGenerator
import java.io.{PrintWriter, StringWriter}

object Jobs extends Controller {
  implicit val logger = Logger(this.getClass())

  val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
  val scheduler = logExceptions(schedulerFactory.getScheduler())
  val dummyClassGenerator = new DummyClassGenerator()

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

  private[controllers] def getDummyJobSource(name: String): String = {
    val writer = new StringWriter()
    val printWriter = new PrintWriter(writer)

    val classNameParts = name.split('.')
    if (classNameParts.length > 1) {
      printWriter.println("package " + classNameParts.clone.dropRight(1).mkString(".") + ";")
    }
    printWriter.println("import org.quartz.Job;")
    printWriter.println("import org.quartz.JobExecutionContext;")
    printWriter.println("import org.quartz.JobExecutionException;")
    printWriter.println("public class " + classNameParts.last + " implements Job {")
    printWriter.println(" public void execute(JobExecutionContext context) throws JobExecutionException {")
    printWriter.println("  }")
    printWriter.println("}")
    printWriter.close()
    writer.toString()
  }

  private[controllers] def getRealOrDummyJob(jobKey: JobKey): Option[JobDetail] = {
    try {
      Some(scheduler.getJobDetail(jobKey))
    }
    catch {
      case e: JobPersistenceException => {
        e.getCause() match {
          case e: ClassNotFoundException => {
            logger.info("Could not find job class " + e.getMessage + ". Trying to generate dummy class now.")
            val source = getDummyJobSource(e.getMessage) //TODO: check how often this is called for a single class
            val job = dummyClassGenerator.generate(e.getMessage, source)
            job.map(jobValue => logger.info("Generated class " + jobValue.getName))
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
