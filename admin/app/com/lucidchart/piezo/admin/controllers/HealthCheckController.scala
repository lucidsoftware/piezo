package com.lucidchart.piezo.admin.controllers

import org.joda.time.{DateTime,Minutes}
import org.joda.time.format.ISODateTimeFormat
import play.api._
import play.api.Play.{current,configuration}
import play.api.mvc._
import scala.io.Source

object HealthCheckController extends Controller {
 def main = Action { implicit requests =>
   if(workersAreHealthy) {
     Ok
   } else {
     ServiceUnavailable
   }

 }

 def workersAreHealthy(): Boolean = {
   val workerTimestamp = (Source.fromFile(configuration.getString("com.lucidchart.piezo.heartbeatFile").get).getLines.toList)(0)
   val formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()
   val heartBeatTime = formatter.parseDateTime(workerTimestamp)
   val currentTime = new DateTime
   Minutes.minutesBetween(heartBeatTime, currentTime).getMinutes < configuration.getInt("healthCheck.worker.minutesBetween").getOrElse(0)
 }
}
