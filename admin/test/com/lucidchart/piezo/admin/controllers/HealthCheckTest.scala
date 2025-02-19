package com.lucidchart.piezo.admin.controllers

import java.io.{File, FileWriter}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.specs2.mutable.*
import org.specs2.specification.AfterEach
import play.api.Configuration
import play.api.test.Helpers.*
import play.api.test.*

class HealthCheckTest extends Specification {

  val filename = "HeartbeatTestFile"
  val dtf = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()

  trait FileCleaner extends After {
    def after: Unit = new File(filename).delete
  }

  private def testConfig(heartbeatFile: String) = Configuration("com.lucidchart.piezo.heartbeatFile" -> heartbeatFile)

  "HealthCheck" should {
    "send 200 when the worker timestamp is recent" in new FileCleaner {
      val file = new File(filename)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.print(new DateTime(System.currentTimeMillis()))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
      val healthCheck = new HealthCheck(testConfig(filename), Helpers.stubControllerComponents())
      val response = healthCheck.main()(FakeRequest())
      status(response) must equalTo(OK)
    }

    "send 503 when the worker timestamp is too far in the past" in new FileCleaner {
      val file = new File(filename)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.print(new DateTime(System.currentTimeMillis()).minusMinutes(10))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
      val healthCheck = new HealthCheck(testConfig(filename), Helpers.stubControllerComponents())
      val response = healthCheck.main()(FakeRequest())
      status(response) must equalTo(SERVICE_UNAVAILABLE)
    }
  }
}
