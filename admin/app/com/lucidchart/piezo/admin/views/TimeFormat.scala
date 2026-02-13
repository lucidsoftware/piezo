package com.lucidchart.piezo.admin.views

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

object TimeFormat {

  private val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(UTC)

  /**
   * Print a java.util.Date in a standard format.
   *
   * Will prilnt "--" if null is passed
   */
  def printDate(d: java.util.Date): String = {
    if (d == null) {
      "--"
    } else {
      dtf.format(d.toInstant)
    }
  }

  /**
   * Print an instant in a standard format.
   */
  def printInstant(i: Instant): String = {
    dtf.format(i)
  }

  def printInstant(instant: Option[Instant]): String = instant match {
    case Some(i) => dtf.format(i)
    case None => "--"
  }
}
