package com.lucidchart.piezo.admin.controllers

import java.io.{File, FileWriter}
import org.specs2.mutable.*
import play.api.Configuration
import play.api.test.Helpers.*
import play.api.test.*
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

class HealthCheckTest extends Specification {

  val filename = "HeartbeatTestFile"
  private val dtf = HealthCheck.timeFormatter

  trait FileCleaner extends After {
    def after: Unit = new File(filename).delete
  }

  private def testConfig(heartbeatFile: String) = Configuration("com.lucidchart.piezo.heartbeatFile" -> heartbeatFile)

  "HealthCheck" should {
    "send 200 when the worker timestamp is recent" in new FileCleaner {
      val file = new File(filename)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.format(Instant.now())
      fileWrite.write(heartbeatTime)
      fileWrite.close()
      val healthCheck = new HealthCheck(testConfig(filename), Helpers.stubControllerComponents())
      val response = healthCheck.main()(FakeRequest())
      status(response) must equalTo(OK)
    }

    "send 503 when the worker timestamp is too far in the past" in new FileCleaner {
      val file = new File(filename)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.format(Instant.now().minus(10, MINUTES))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
      val healthCheck = new HealthCheck(testConfig(filename), Helpers.stubControllerComponents())
      val response = healthCheck.main()(FakeRequest())
      status(response) must equalTo(SERVICE_UNAVAILABLE)
    }
  }
}
