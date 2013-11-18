package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import com.lucidchart.piezo.WorkerSchedulerFactory
import play.api.test.FakeApplication
import TestUtil._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
class TriggersSpec extends Specification {
   "Application" should {

     "send 404 on a non-existent trigger request" in {
       running(FakeApplication()) {
         val missingJob = route(FakeRequest(GET, "/triggers/missinggroup/missingname")).get

         status(missingJob) must equalTo(NOT_FOUND)
         contentType(missingJob) must beSome.which(_ == "text/html")
         contentAsString(missingJob) must contain ("Trigger missinggroup missingname not found")
       }
     }

     "send valid trigger details" in {
       val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
       val scheduler = schedulerFactory.getScheduler()
       createJob(scheduler)

       running(FakeApplication()) {
         val missingJob = route(FakeRequest(GET, "/triggers/" + triggerGroup + "/" + triggerName)).get

         status(missingJob) must equalTo(OK)
         contentType(missingJob) must beSome.which(_ == "text/html")
         contentAsString(missingJob) must contain (triggerGroup)
         contentAsString(missingJob) must contain (triggerName)
       }
     }
   }
 }