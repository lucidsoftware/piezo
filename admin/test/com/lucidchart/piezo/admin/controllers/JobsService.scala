package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import com.lucidchart.piezo.jobs.monitoring.HeartBeat
import com.lucidchart.piezo.WorkerSchedulerFactory
import org.quartz._
import play.api.test.FakeApplication
import TestUtil._
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import com.lucidchart.piezo.util.DummyClassGenerator

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
class JobsService extends Specification {
  val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
  rootLogger.setLevel(Level.DEBUG)

   "Jobs" should {

     "send 404 on a non-existent job request" in {
       running(FakeApplication()) {
         val missingJob = route(FakeRequest(GET, "/jobs/missinggroup/missingname")).get

         status(missingJob) must equalTo(NOT_FOUND)
         contentType(missingJob) must beSome.which(_ == "text/html")
         contentAsString(missingJob) must contain ("Job missinggroup missingname not found")
       }
     }

     "send valid job details" in {
       val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
       val scheduler = schedulerFactory.getScheduler()
       createJob(scheduler)

       running(FakeApplication()) {
         val validJob = route(FakeRequest(GET, "/jobs/" + jobGroup + "/" + jobName)).get

         status(validJob) must equalTo(OK)
         contentType(validJob) must beSome.which(_ == "text/html")
         contentAsString(validJob) must contain (classOf[HeartBeat].getName())
       }
     }
   }
}