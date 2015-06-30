package com.lucidchart.piezo.admin.controllers

import org.joda.time.{DateTime,Minutes}
import org.joda.time.format.ISODateTimeFormat
import play.api._
import play.api.Play.{current,configuration}
import play.api.mvc._
import scala.io.Source

object HealthCheck extends Controller {
  implicit val logger = Logger(this.getClass())

  val heartbeatFilename = configuration.getString("com.lucidchart.piezo.heartbeatFile").getOrElse {
    logger.warn("heartbeat file not specified")
    ""
  }
  val minutesBetweenBeats = configuration.getInt("healthCheck.worker.minutesBetween").getOrElse{
    logger.warn("minutes between heartbeats not specified. Defaulting to 5")
    5
  }

  def main = Action { implicit requests =>
   if(workersAreHealthy) {
     Ok
   } else {
     ServiceUnavailable
   }

 }

  def workersAreHealthy(): Boolean = {
    val heartbeatFile = Source.fromFile(heartbeatFilename).getLines.toList
    val heartbeatTimestamp = heartbeatFile(0)
    val formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()
    val heartbeatTime = formatter.parseDateTime(heartbeatTimestamp)
    val currentTime = new DateTime
    Minutes.minutesBetween(heartbeatTime, currentTime).getMinutes < minutesBetweenBeats
 }
}
