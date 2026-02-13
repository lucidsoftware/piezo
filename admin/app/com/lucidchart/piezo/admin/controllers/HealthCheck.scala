package com.lucidchart.piezo.admin.controllers

import play.api.*
import play.api.libs.json.*
import play.api.Logging
import play.api.mvc.*
import scala.io.Source
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.{MINUTES, SECONDS}

class HealthCheck(configuration: Configuration, cc: ControllerComponents) extends AbstractController(cc) with Logging {
  import HealthCheck.timeFormatter

  val heartbeatFilename: String = configuration.getOptional[String]("com.lucidchart.piezo.heartbeatFile").getOrElse {
    logger.warn("heartbeat file not specified")
    ""
  }
  val minutesBetweenBeats: Int = configuration.getOptional[Int]("healthCheck.worker.minutesBetween").getOrElse {
    logger.warn("minutes between heartbeats not specified. Defaulting to 5")
    5
  }

  def main: Action[AnyContent] = cc.actionBuilder { requests =>
    val workerHealth = areWorkersHealthy()
    val responseBody = Json.toJson(Map("HeartbeatTime" -> Json.toJson(workerHealth._2)))
    if (workerHealth._1) {
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
      val heartbeatTime = timeFormatter.parse(heartbeatTimestamp, Instant.from)
      val currentTime = Instant.now()
      val isTimestampRecent = heartbeatTime.until(currentTime, MINUTES) < minutesBetweenBeats
      (isTimestampRecent, timeFormatter.format(heartbeatTime.truncatedTo(SECONDS)))
    } finally {
      heartbeatFile.close()
    }
  }
}

object HealthCheck {
  private[piezo] val timeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(UTC)
}
