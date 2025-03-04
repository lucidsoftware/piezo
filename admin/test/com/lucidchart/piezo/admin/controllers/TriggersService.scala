package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable.*
import play.api.test.*
import play.api.test.Helpers.*
import com.lucidchart.piezo.WorkerSchedulerFactory
import TestUtil.*
import java.util.Properties
import play.api.mvc.{AnyContentAsEmpty, Result}
import scala.concurrent.Future
import com.lucidchart.piezo.admin.models.MonitoringTeams

/**
 * Add your spec here. You can mock out a whole application including requests, plugins etc. For more information,
 * consult the wiki.
 */
class TriggersService extends Specification {
  "Triggers" should {

    "send 404 on a non-existent trigger request" in {
      val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()

      val propertiesStream = getClass().getResourceAsStream("/quartz_test.properties")
      val properties = new Properties
      properties.load(propertiesStream)
      schedulerFactory.initialize(properties)

      val triggersController =
        new Triggers(
          schedulerFactory.getScheduler(),
          TestUtil.mockModelComponents,
          Helpers.stubControllerComponents(),
          MonitoringTeams.empty,
        )
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/triggers/missinggroup/missingname")
      val missingTrigger: Future[Result] = triggersController.getTrigger("missinggroup", "missingname")(request)

      status(missingTrigger) must equalTo(NOT_FOUND)
      contentType(missingTrigger) must beSome("text/html")
      contentAsString(missingTrigger) must contain("Trigger missinggroup missingname not found")
    }

    "send valid trigger details" in {
      val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
      val propertiesStream = getClass().getResourceAsStream("/quartz_test.properties")
      val properties = new Properties
      properties.load(propertiesStream)
      schedulerFactory.initialize(properties)
      val scheduler = schedulerFactory.getScheduler()
      createJob(scheduler)

      val triggersController =
        new Triggers(scheduler, TestUtil.mockModelComponents, Helpers.stubControllerComponents(), MonitoringTeams.empty)
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/triggers/" + jobGroup + "/" + jobName)
      val validTrigger: Future[Result] = triggersController.getTrigger(triggerGroup, triggerName)(request)

      status(validTrigger) must equalTo(OK)
      contentType(validTrigger) must beSome("text/html")
      contentAsString(validTrigger) must contain(triggerGroup)
      contentAsString(validTrigger) must contain(triggerName)
    }
  }
}
