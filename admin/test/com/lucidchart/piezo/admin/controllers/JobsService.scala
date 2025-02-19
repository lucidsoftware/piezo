package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable.*
import play.api.test.*
import play.api.test.Helpers.*
import com.lucidchart.piezo.jobs.monitoring.HeartBeat
import com.lucidchart.piezo.WorkerSchedulerFactory
import TestUtil.*
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import play.api.mvc.{AnyContentAsEmpty, Result}
import java.util.Properties
import play.api.Configuration
import scala.concurrent.Future
import com.lucidchart.piezo.admin.models.MonitoringTeams

/**
 * Add your spec here. You can mock out a whole application including requests, plugins etc. For more information,
 * consult the wiki.
 */
class JobsService extends Specification {
  val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
  rootLogger.setLevel(Level.DEBUG)

  private val jobView = new com.lucidchart.piezo.admin.views.html.job(Configuration.empty)

  "Jobs" should {

    "send 404 on a non-existent job request" in {
      val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()

      val propertiesStream = getClass().getResourceAsStream("/quartz_test.properties")
      val properties = new Properties
      properties.load(propertiesStream)
      schedulerFactory.initialize(properties)
      val scheduler = schedulerFactory.getScheduler()

      val jobsController =
        new Jobs(
          scheduler,
          TestUtil.mockModelComponents,
          jobView,
          Helpers.stubControllerComponents(),
          MonitoringTeams.empty,
        )
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/jobs/missinggroup/missingname")
      val missingJob: Future[Result] = jobsController.getJob("missinggroup", "missingname")(request)

      status(missingJob) must equalTo(NOT_FOUND)
      contentType(missingJob) must beSome("text/html")
      contentAsString(missingJob) must contain("Job missinggroup missingname not found")
    }

    "send valid job details" in {
      val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
      val propertiesStream = getClass().getResourceAsStream("/quartz_test.properties")
      val properties = new Properties
      properties.load(propertiesStream)
      schedulerFactory.initialize(properties)
      val scheduler = schedulerFactory.getScheduler()
      createJob(scheduler)

      val jobsController =
        new Jobs(
          scheduler,
          TestUtil.mockModelComponents,
          jobView,
          Helpers.stubControllerComponents(),
          MonitoringTeams.empty,
        )
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/jobs/" + jobGroup + "/" + jobName)
      val validJob: Future[Result] = jobsController.getJob(jobGroup, jobName)(request)

      status(validJob) must equalTo(OK)
      contentType(validJob) must beSome("text/html")
      contentAsString(validJob) must contain(classOf[HeartBeat].getName())
      contentAsString(validJob) must contain(s"triggers/new/cron?jobGroup=$jobGroup&jobName=$jobName")
    }
  }
}
