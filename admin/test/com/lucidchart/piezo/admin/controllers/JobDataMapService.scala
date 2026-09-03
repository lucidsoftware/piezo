package com.lucidchart.piezo.admin.controllers

import ch.qos.logback.classic.{Level, Logger}
import com.lucidchart.piezo.WorkerSchedulerFactory
import com.lucidchart.piezo.admin.models.MonitoringTeams
import com.lucidchart.piezo.jobs.monitoring.HeartBeat
import java.util.Properties
import org.quartz.{JobBuilder, JobKey, Scheduler, SimpleScheduleBuilder, TriggerBuilder, TriggerKey}
import org.slf4j.LoggerFactory
import org.specs2.mutable.*
import play.api.Configuration
import play.api.libs.json.*
import play.api.mvc.Result
import play.api.test.*
import play.api.test.Helpers.*
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class JobDataMapService extends Specification {
  val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
  rootLogger.setLevel(Level.DEBUG)

  private val jobView = new com.lucidchart.piezo.admin.views.html.job(Configuration.empty)
  private val group = "dataMapTestGroup"

  private def newScheduler(): Scheduler = {
    val schedulerFactory = new WorkerSchedulerFactory()
    val properties = new Properties
    properties.load(getClass().getResourceAsStream("/quartz_test.properties"))
    schedulerFactory.initialize(properties)
    schedulerFactory.getScheduler()
  }

  private def controller(scheduler: Scheduler) =
    new Jobs(
      scheduler,
      TestUtil.mockModelComponents,
      jobView,
      Helpers.stubControllerComponents(),
      MonitoringTeams.empty,
    )

  /**
   * Creates a durable job with the given data map, plus a trigger, so tests can check that a patch leaves the rest of
   * the job alone.
   */
  private def createJob(
    scheduler: Scheduler,
    name: String,
    data: Map[String, String],
    durable: Boolean = true,
  ): JobKey = {
    val jobDetail = JobBuilder
      .newJob(classOf[HeartBeat])
      .withIdentity(name, group)
      .withDescription("a job with a data map")
      .storeDurably(durable)
      .requestRecovery(true)
      .usingJobData(new org.quartz.JobDataMap(data.asJava))
      .build()
    val trigger = TriggerBuilder.newTrigger
      .withIdentity(name, group)
      .forJob(jobDetail)
      .withSchedule(SimpleScheduleBuilder.simpleSchedule.withIntervalInSeconds(5).withRepeatCount(1))
      .build()
    scheduler.deleteJob(jobDetail.getKey)
    scheduler.scheduleJob(jobDetail, trigger)
    jobDetail.getKey
  }

  private def patch(scheduler: Scheduler, body: JsValue): Future[Result] = {
    val request = FakeRequest(PATCH, "/data/jobs/job-data-map").withBody(body)
    controller(scheduler).patchJobDataMaps(request)
  }

  private def dataMapOf(scheduler: Scheduler, jobKey: JobKey): Map[String, AnyRef] = {
    scheduler.getJobDetail(jobKey).getJobDataMap.asScala.toMap
  }

  "patchJobDataMaps" should {

    "merge the given keys into the existing data map" in {
      val scheduler = newScheduler()
      val jobKey = createJob(scheduler, "merge", Map("lastRunTime" -> "100", "queue" -> "documents"))

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj(
              "group" -> group,
              "name" -> "merge",
              "job-data-map" -> Json.obj("lastRunTime" -> "50"),
            ),
          ),
        ),
      )

      status(result) must equalTo(OK)
      (contentAsJson(result) \ "count").as[Int] must equalTo(1)
      (contentAsJson(result) \ "failures").as[JsArray].value must beEmpty
      dataMapOf(scheduler, jobKey) must equalTo(Map("lastRunTime" -> "50", "queue" -> "documents"))
    }

    "leave the rest of the job and its triggers alone" in {
      val scheduler = newScheduler()
      val jobKey = createJob(scheduler, "untouched", Map("lastRunTime" -> "100"))

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj("group" -> group, "name" -> "untouched", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
          ),
        ),
      )

      status(result) must equalTo(OK)
      val jobDetail = scheduler.getJobDetail(jobKey)
      jobDetail.getDescription must equalTo("a job with a data map")
      jobDetail.getJobClass must equalTo(classOf[HeartBeat])
      jobDetail.isDurable must beTrue
      jobDetail.requestsRecovery must beTrue
      scheduler.getTriggersOfJob(jobKey).asScala.map(_.getKey) must equalTo(
        Seq(new TriggerKey("untouched", group)),
      )
    }

    "refuse to remove a key, leaving the job untouched" in {
      val scheduler = newScheduler()
      val jobKey = createJob(scheduler, "remove", Map("lastRunTime" -> "100", "stale" -> "yes"))

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj("group" -> group, "name" -> "remove", "job-data-map" -> Json.obj("stale" -> JsNull)),
          ),
        ),
      )

      status(result) must equalTo(BAD_REQUEST)
      dataMapOf(scheduler, jobKey) must equalTo(Map("lastRunTime" -> "100", "stale" -> "yes"))
    }

    "patch every job in one request" in {
      val scheduler = newScheduler()
      val first = createJob(scheduler, "bulk1", Map("lastRunTime" -> "100"))
      val second = createJob(scheduler, "bulk2", Map("lastRunTime" -> "200"))

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj("group" -> group, "name" -> "bulk1", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
            Json.obj("group" -> group, "name" -> "bulk2", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
          ),
        ),
      )

      status(result) must equalTo(OK)
      (contentAsJson(result) \ "updated").as[JsArray].value must haveSize(2)
      dataMapOf(scheduler, first) must equalTo(Map("lastRunTime" -> "50"))
      dataMapOf(scheduler, second) must equalTo(Map("lastRunTime" -> "50"))
    }

    "report a missing job without skipping the rest" in {
      val scheduler = newScheduler()
      val jobKey = createJob(scheduler, "present", Map("lastRunTime" -> "100"))
      scheduler.deleteJob(new JobKey("absent", group))

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj("group" -> group, "name" -> "absent", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
            Json.obj("group" -> group, "name" -> "present", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
          ),
        ),
      )

      status(result) must equalTo(MULTI_STATUS)
      val json = contentAsJson(result)
      (json \ "count").as[Int] must equalTo(2)
      (json \ "updated").as[JsArray].value must haveSize(1)
      (json \ "failures" \ 0 \ "jobName").as[String] must equalTo("absent")
      (json \ "failures" \ 0 \ "errorMessage").as[String] must contain("not found")
      dataMapOf(scheduler, jobKey) must equalTo(Map("lastRunTime" -> "50"))
    }

    "patch a non durable job that is only kept around by its trigger" in {
      val scheduler = newScheduler()
      val jobKey = createJob(scheduler, "nonDurable", Map("lastRunTime" -> "100"), durable = false)

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj("group" -> group, "name" -> "nonDurable", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
          ),
        ),
      )

      status(result) must equalTo(OK)
      dataMapOf(scheduler, jobKey) must equalTo(Map("lastRunTime" -> "50"))
      scheduler.getJobDetail(jobKey).isDurable must beFalse
      scheduler.getTriggersOfJob(jobKey).asScala must haveSize(1)
    }

    "apply nothing when the body is malformed" in {
      val scheduler = newScheduler()
      val jobKey = createJob(scheduler, "malformed", Map("lastRunTime" -> "100"))

      val result = patch(
        scheduler,
        Json.obj(
          "jobs" -> Json.arr(
            Json.obj("group" -> group, "name" -> "malformed", "job-data-map" -> Json.obj("lastRunTime" -> "50")),
            Json.obj("group" -> group, "name" -> "no-data-map-here"),
          ),
        ),
      )

      status(result) must equalTo(BAD_REQUEST)
      dataMapOf(scheduler, jobKey) must equalTo(Map("lastRunTime" -> "100"))
    }
  }
}
