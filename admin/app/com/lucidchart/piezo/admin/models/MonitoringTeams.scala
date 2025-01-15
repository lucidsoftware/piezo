package com.lucidchart.piezo.admin.models

import play.api.Configuration
import java.nio.file.Files
import play.api.libs.json.Json
import scala.util.Try
import java.io.File
import java.io.FileInputStream
import play.api.libs.json.JsArray
import play.api.Logging
import scala.util.control.NonFatal
import scala.util.Failure

case class MonitoringTeams(value: Seq[String]) {
  def teamsDefined: Boolean = value.nonEmpty
}
object MonitoringTeams extends Logging {
  def apply(configuration: Configuration): MonitoringTeams = {
    val path = configuration.getOptional[String]("com.lucidchart.piezo.admin.monitoringTeams.path")

    val value = path.flatMap { p =>
      Try {
        Json.parse(new FileInputStream(p))
          .as[JsArray]
          .value
          .map(entry => (entry \ "name").as[String])
          .toSeq
      }.recoverWith {
        case NonFatal(e) =>
          logger.error(s"Error reading monitoring teams from $p", e)
          Failure(e)
      }.toOption
    }.getOrElse(Seq.empty)

    MonitoringTeams(value)
  }

  def empty: MonitoringTeams = MonitoringTeams(Seq.empty)
}
