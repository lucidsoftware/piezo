package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
class JobsSpec extends Specification {

   "Application" should {

     "send 404 on a non-existenet job request" in {
       running(FakeApplication()) {
         val missingJob = route(FakeRequest(GET, "/jobs/missinggroup/missingname")).get

         status(missingJob) must equalTo(NOT_FOUND)
         contentType(missingJob) must beSome.which(_ == "text/html")
         contentAsString(missingJob) must contain ("Job missinggroup/missingname not found")
       }
     }

     "send valid job details" in {
       running(FakeApplication()) {
         val missingJob = route(FakeRequest(GET, "/jobs/testgroup/testname")).get

         status(missingJob) must equalTo(OK)
         contentType(missingJob) must beSome.which(_ == "text/html")
         //contentAsString(missingJob) must contain ("testgroup")
       }
     }
   }
 }