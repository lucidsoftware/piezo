package com.lucidchart.piezo.admin.controllers

import org.joda.time.{DateTime,Minutes}
import org.joda.time.format.ISODateTimeFormat
import play.api._
import play.api.libs.json._
import play.api.Logging
import play.api.mvc._
import scala.io.Source

class HealthCheck(configuration: Configuration, cc: ControllerComponents) extends AbstractController(cc) with Logging {

  val heartbeatFilename = configuration.getOptional[String]("com.lucidchart.piezo.heartbeatFile").getOrElse {
    logger.warn("heartbeat file not specified")
    ""
  }
  val minutesBetweenBeats = configuration.getOptional[Int]("healthCheck.worker.minutesBetween").getOrElse{
    logger.warn("minutes between heartbeats not specified. Defaulting to 5")
    5
  }

  def main = cc.actionBuilder { implicit requests=>
    val workerHealth = areWorkersHealthy()
    val responseBody = Json.toJson(Map("HeartbeatTime" -> Json.toJson(workerHealth._2)))
    if(workerHealth._1) {
     Ok(responseBody)
    } else {
     ServiceUnavailable(responseBody)
    }
 }

  def areWorkersHealthy(): (Boolean, String) = {
    val heartbeatFile = Source.fromFile(heartbeatFilename)
    try {
      val heartbeatFileLines = heartbeatFile.getLines().toList
      val heartbeatTimestamp = heartbeatFileLines(0)
      val formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()
      val heartbeatTime = formatter.parseDateTime(heartbeatTimestamp)
      val currentTime = new DateTime
      val isTimestampRecent = Minutes.minutesBetween(heartbeatTime, currentTime).getMinutes < minutesBetweenBeats
      (isTimestampRecent, formatter.print(heartbeatTime))
    }
    finally {
      heartbeatFile.close()
    }
 }
}
