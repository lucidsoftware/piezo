package com.lucidchart.piezo.admin.controllers

import com.lucidchart.piezo.admin.utils.JobUtils
import com.lucidchart.piezo.admin.utils.JobDetailHelper.*
import com.lucidchart.piezo.admin.views.*
import com.lucidchart.piezo.{JobHistoryModel, TriggerHistoryModel, TriggerMonitoringModel, WorkerSchedulerFactory}
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import play.api.*
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.mvc.*
import scala.jdk.CollectionConverters.*
import scala.collection.mutable
import scala.Some
import scala.io.Source
import com.lucidchart.piezo.admin.models.MonitoringTeams

trait ImportResult {
  val jobKey: Option[JobKey]
  val errorMessage: String
  val success: Boolean
  def toJson: JsObject = {
    Json.obj(
      "success" -> success,
      "errorMessage" -> errorMessage,
    ) ++ jobKey
      .map { jk =>
        Json.obj(
          "jobName" -> jk.getName,
          "jobGroup" -> jk.getGroup,
        )
      }
      .getOrElse(Json.obj())
  }
}

case class ImportSuccess(val jobKey: Option[JobKey], val errorMessage: String = "", val success: Boolean = true)
    extends ImportResult
case class ImportFailure(val jobKey: Option[JobKey], val errorMessage: String, val success: Boolean = false)
    extends ImportResult

class Jobs(
  schedulerFactory: WorkerSchedulerFactory,
  jobView: html.job,
  cc: ControllerComponents,
  monitoringTeams: MonitoringTeams,
) extends AbstractController(cc)
    with Logging
    with ErrorLogging
    with play.api.i18n.I18nSupport {
  val scheduler: Scheduler = logExceptions(schedulerFactory.getScheduler())
  val properties = schedulerFactory.props
  val jobHistoryModel: JobHistoryModel = logExceptions(new JobHistoryModel(properties))
  val triggerMonitoringPriorityModel: TriggerMonitoringModel = logExceptions(new TriggerMonitoringModel(properties))

  val jobFormHelper = new JobFormHelper()
  val triggerFormHelper = new TriggerFormHelper(scheduler, monitoringTeams)

  // Allow up to 1M
  private val maxFormSize = 1024 * 1024

  def getJobsByGroup(): mutable.Buffer[(String, List[JobKey])] = {
    val jobsByGroup =
      for (groupName <- scheduler.getJobGroupNames().asScala) yield {
        val jobs: List[JobKey] = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).asScala.toList
        val sortedJobs: List[JobKey] = jobs.sortBy(jobKey => jobKey.getName())
        (groupName, sortedJobs)
      }
    jobsByGroup.sortBy(groupList => groupList._1)
  }

  def getIndex: Action[AnyContent] = Action { implicit request =>
    val allJobs: List[JobKey] = getJobsByGroup().flatMap(_._2).toList
    val jobHistories = allJobs
      .flatMap { job =>
        jobHistoryModel.getJob(job).headOption
      }
      .sortWith(_.start after _.start)
    val triggeredJobs: List[JobKey] = TriggerHelper
      .getTriggersByGroup(scheduler)
      .flatMap { case (group, triggerKeys) =>
        triggerKeys.map(triggerKey => scheduler.getTrigger(triggerKey).getJobKey)
      }
      .toList
    val untriggeredJobs: List[JobKey] = allJobs.filterNot(x => triggeredJobs.contains(x))
    Ok(
      com.lucidchart.piezo.admin.views.html
        .jobs(getJobsByGroup(), None, Some(jobHistories), untriggeredJobs, scheduler.getMetaData)(request),
    )
  }

  def getJob(group: String, name: String): Action[AnyContent] = Action { implicit request =>
    val jobKey = new JobKey(name, group)
    val jobExists = scheduler.checkExists(jobKey)
    if (!jobExists) {

      request.accepts(HTML)
      val errorMsg = Some("Job " + group + " " + name + " not found")
      NotFound(jobView(getJobsByGroup(), None, None, None, errorMsg)(request))
    } else {
      try {
        val jobDetail: Option[JobDetail] = Some(scheduler.getJobDetail(jobKey))

        val history = {
          try {
            Some(jobHistoryModel.getJob(jobKey))
          } catch {
            case e: Exception => {
              logger.error("Failed to get job history")
              None
            }
          }
        }

        val triggers = scheduler.getTriggersOfJob(jobKey).asScala.toList
        val (resumableTriggers, pausableTriggers) =
          triggers.filter(_.isInstanceOf[CronTrigger]).map(_.getKey()).partition { triggerKey =>
            scheduler.getTriggerState(triggerKey) == Trigger.TriggerState.PAUSED
          }

        Ok(
          jobView(getJobsByGroup(), jobDetail, history, Some(triggers), None, pausableTriggers, resumableTriggers)(
            request,
          ),
        )
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught getting job " + group + " " + name + ". -- " + e.getLocalizedMessage()
          logger.error(errorMsg, e)
          InternalServerError(jobView(getJobsByGroup(), None, None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  def deleteJob(group: String, name: String): Action[AnyContent] = Action { implicit request =>
    val jobKey = new JobKey(name, group)
    if (!scheduler.checkExists(jobKey)) {
      val errorMsg = Some("Job %s $s not found".format(group, name))
      NotFound(jobView(mutable.Buffer(), None, None, None, errorMsg)(request))
    } else {
      try {
        scheduler.deleteJob(jobKey)
        Ok(jobView(getJobsByGroup(), None, None, None)(request))
      } catch {
        case e: Exception => {
          val errorMsg = "Exception caught deleting job %s %s. -- %s".format(group, name, e.getLocalizedMessage())
          logger.error(errorMsg, e)
          InternalServerError(jobView(mutable.Buffer(), None, None, None, Some(errorMsg))(request))
        }
      }
    }
  }

  def getJobDetail(group: String, name: String): Action[AnyContent] = Action { implicit request =>
    if (request.accepts(JSON)) {
      val jobKey = new JobKey(name, group)
      if (scheduler.checkExists(jobKey)) {
        val jobDetail = scheduler.getJobDetail(jobKey)
        val triggers = scheduler.getTriggersOfJob(jobKey).asScala.toList
        Ok(Json.toJson(Seq(jobDetail))(jobDetailSeqWrites(triggers, triggerMonitoringPriorityModel)))
      } else {
        NotFound(Json.obj("message" -> "specified job doesn't exist"))
      }
    } else {
      UnsupportedMediaType
    }
  }

  def getJobsDetail: Action[AnyContent] = Action { implicit request =>
    if (request.accepts(JSON)) {
      Ok(
        Json.toJson(
          scheduler.getJobGroupNames().asScala.toList.flatMap { group =>
            val jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).asScala
            jobKeys.map { jk =>
              val triggers = scheduler.getTriggersOfJob(jk).asScala.toList
              Json.toJson(scheduler.getJobDetail(jk))(jobDetailWrites(triggers, triggerMonitoringPriorityModel))
            }
          },
        ),
      )
    } else {
      UnsupportedMediaType
    }
  }

  val submitNewMessage = "Create"
  val formNewAction = routes.Jobs.postJob
  val submitEditMessage = "Save"
  def formEditAction(group: String, name: String): Call = routes.Jobs.putJob(group, name)

  def getNewJobForm(templateGroup: Option[String] = None, templateName: Option[String] = None): Action[AnyContent] =
    Action { implicit request =>
      // if (request.queryString.contains())
      templateGroup match {
        case Some(group) => getEditJob(group, templateName.get, true)
        case None =>
          val newJobForm = jobFormHelper.buildJobForm
          Ok(
            com.lucidchart.piezo.admin.views.html
              .editJob(getJobsByGroup(), newJobForm, submitNewMessage, formNewAction, false)(request, implicitly),
          )
      }

    }

  def getEditJob(group: String, name: String, isTemplate: Boolean)(implicit request: Request[AnyContent]): Result = {
    val jobKey = new JobKey(name, group)

    if (scheduler.checkExists(jobKey)) {
      val jobDetail = scheduler.getJobDetail(jobKey)
      val editJobForm = jobFormHelper.buildJobForm.fill(jobDetail)
      if (isTemplate)
        Ok(
          com.lucidchart.piezo.admin.views.html
            .editJob(getJobsByGroup(), editJobForm, submitNewMessage, formNewAction, false)(request, implicitly),
        )
      else
        Ok(
          com.lucidchart.piezo.admin.views.html.editJob(
            getJobsByGroup(),
            editJobForm,
            submitEditMessage,
            formEditAction(group, name),
            true,
          )(request, implicitly),
        )
    } else {
      val errorMsg = Some("Job %s %s not found".format(group, name))
      NotFound(com.lucidchart.piezo.admin.views.html.trigger(mutable.Buffer(), None, None, errorMsg)(request))
    }
  }

  def getEditJobAction(group: String, name: String): Action[AnyContent] = Action { implicit request =>
    getEditJob(group, name, false)
  }

  def putJob(group: String, name: String): Action[AnyContent] = Action { implicit request =>
    jobFormHelper.buildJobForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          BadRequest(html.editJob(getJobsByGroup(), formWithErrors, submitNewMessage, formNewAction, false)),
        value => {
          val jobDetail = JobUtils.cleanup(value)
          scheduler.addJob(jobDetail, true)
          Redirect(routes.Jobs.getJob(value.getKey.getGroup(), value.getKey.getName()))
            .flashing("message" -> "Successfully edited job.", "class" -> "")
        },
      )
  }

  def parseJson(json: JsValue): List[ImportResult] = {
    def formErrorStr(formErrors: Seq[play.api.data.FormError]): String = {
      formErrors
        .map { e =>
          (if (e.key.nonEmpty) s"${e.key}:" else "") + e.message
        }
        .mkString(",")
    }

    json.as[List[JsObject]].map { jsObject =>
      jobFormHelper.buildJobForm
        .bind(jsObject, maxFormSize)
        .fold(
          e => {
            val jobKey = for {
              name <- (jsObject \ "name").validate[String]
              group <- (jsObject \ "group").validate[String]
            } yield {
              new JobKey(name, group)
            }
            val errorMessage = formErrorStr(e.errors)
            logger.error(errorMessage)
            ImportFailure(jobKey.asOpt, "Job Import Error:" + errorMessage)
          },
          value => {
            try {
              val jobDetail = JobUtils.cleanup(value)
              scheduler.addJob(jobDetail, false)
              val triggersOpt = (jsObject \ "triggers").asOpt[List[JsObject]]
              val triggersBinding =
                triggersOpt.map(_.map(triggerFormHelper.buildTriggerForm.bind(_, maxFormSize))).getOrElse(Nil)
              if (triggersBinding.exists(b => b.hasErrors || b.hasGlobalErrors)) {
                val errorMessage = formErrorStr(triggersBinding.filter(_.hasErrors).flatMap(_.errors))
                logger.error(errorMessage)
                ImportFailure(Some(jobDetail.getKey), "Trigger Import Error:" + errorMessage)
              } else {
                val triggers = triggersBinding.flatMap(_.value)
                triggers.foreach { case TriggerFormValue(trigger, monitoringPriority, errorTime, monitoringTeam) =>
                  scheduler.scheduleJob(trigger)
                  triggerMonitoringPriorityModel.setTriggerMonitoringRecord(
                    trigger.getKey,
                    monitoringPriority,
                    errorTime,
                    monitoringTeam,
                  )
                }
                ImportSuccess(Some(jobDetail.getKey))
              }
            } catch {
              case _: ObjectAlreadyExistsException =>
                logger.error(
                  s"Failed to add Job ${value.getKey.getGroup} - ${value.getKey.getGroup} because it already exists",
                )
                ImportFailure(Some(value.getKey), "The Job Already Exists")
            }
          },
        )
    }
  }

  def postJobs: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val jsonOpt = request.body.asJson.orElse {
      request.body.asMultipartFormData.flatMap { d =>
        d.file("file").map { f =>
          Json.parse(Source.fromFile(f.ref.path.toFile()).getLines().mkString)
        }
      }
    }
    jsonOpt
      .map { json =>
        val results = parseJson(json)
        if (results.forall(_.success == true)) {
          Created(
            Json.obj(
              "count" -> results.size,
              "failures" -> Json.arr(),
            ),
          )
        } else {
          Created(
            Json.obj(
              "count" -> results.size,
              "failures" -> results.filter(_.success == false).map(_.toJson),
            ),
          )
        }
      }
      .getOrElse {
        BadRequest
      }
  }

  def postJob: Action[AnyContent] = Action { implicit request =>
    jobFormHelper.buildJobForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          BadRequest(
            com.lucidchart.piezo.admin.views.html
              .editJob(getJobsByGroup(), formWithErrors, submitNewMessage, formNewAction, false),
          ),
        value => {
          try {
            val jobDetail = JobUtils.cleanup(value)
            scheduler.addJob(jobDetail, false)
            Redirect(routes.Jobs.getJob(value.getKey.getGroup(), value.getKey.getName()))
              .flashing("message" -> "Successfully added job.", "class" -> "")
          } catch {
            case alreadyExists: ObjectAlreadyExistsException =>
              val form = jobFormHelper.buildJobForm.fill(value)
              Ok(
                com.lucidchart.piezo.admin.views.html.editJob(
                  getJobsByGroup(),
                  form,
                  submitNewMessage,
                  formNewAction,
                  false,
                  errorMessage = Some("Please provide unique group-name pair"),
                )(request, implicitly),
              )
          }
        },
      )
  }

  def jobGroupTypeAhead(sofar: String): Action[AnyContent] = Action { implicit request =>
    val groups = scheduler.getJobGroupNames().asScala.toList

    Ok(Json.obj("groups" -> groups.filter { group =>
      group.toLowerCase.contains(sofar.toLowerCase)
    }))
  }

  def jobNameTypeAhead(group: String, sofar: String): Action[AnyContent] = Action { implicit request =>
    val jobs = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).asScala.toSet

    Ok(Json.obj("jobs" -> jobs.filter(_.getName.toLowerCase.contains(sofar.toLowerCase)).map(_.getName)))
  }

}
