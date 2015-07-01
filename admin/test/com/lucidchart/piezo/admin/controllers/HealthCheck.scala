package test.com.lucidchart.piezo.admin.controllers

import java.io.{FileWriter, File}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.specs2.mutable._
import org.specs2.specification.AfterEach
import play.api.test._
import play.api.test.Helpers._


class HealthCheck extends Specification {

  val filename = "HeartbeatTestFile"
  val dtf = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()

  trait FileCleaner extends After {
    def after = new File(filename).delete
  }


  "HealthCheck" should {
    "send 200 when the worker timestamp is recent" in new FileCleaner{
      val file = new File(filename)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.print(new DateTime(System.currentTimeMillis()))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
      running(FakeApplication(additionalConfiguration = Map(("com.lucidchart.piezo.heartbeatFile",filename)))) {
        val response = route(FakeRequest(GET, "/health")).get
        status(response) must equalTo(OK)
      }
    }

    "send 503 when the worker timestamp is too far in the past" in new FileCleaner{
      val file = new File(filename)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.print(new DateTime(System.currentTimeMillis()).minusMinutes(10))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
      running(FakeApplication(additionalConfiguration = Map(("com.lucidchart.piezo.heartbeatFile",filename)))) {
        val response = route(FakeRequest(GET, "/health")).get
        status(response) must equalTo(SERVICE_UNAVAILABLE)
      }
    }

  }
}

