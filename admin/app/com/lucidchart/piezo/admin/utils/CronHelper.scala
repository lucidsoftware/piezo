package com.lucidchart.piezo.admin.utils

import java.util.{Date, TimeZone}
import org.quartz.{CronExpression, CronScheduleBuilder, CronTrigger, TriggerBuilder}
import play.api.Logging
import scala.util.control.NonFatal

object CronHelper extends Logging {
  val DEFAULT_MAX_INTERVAL = 0
  val NUM_COMPLEX_SAMPLES = 8000

  /**
   * Determines the max interval for a cron expression. The operation fails silently because it's not crucial.
   * @param cronExpression
   */
  def getMaxInterval(cronExpression: CronExpression): Long = getMaxInterval(cronExpression, None)
  def getMaxInterval(cronExpression: CronExpression, timeZone: Option[TimeZone]): Long = {
    try {
      val subexpressions = cronExpression.getCronExpression.split("\\s")
      val isComplex = !subexpressions.drop(3).forall(expr => expr == "*" || expr == "?")
      if (isComplex) getComplexMaxInterval(subexpressions, timeZone) else getSimpleMaxInterval(subexpressions, timeZone)
    } catch {
      case NonFatal(e) =>
        logger.error("Failed to validate cron expression", e)
        DEFAULT_MAX_INTERVAL
    }
  }

  /**
   * Determines the max interval by deduction. Is only used when seconds, minutes, and hours are specified. If days,
   * months, or years are specified, use getComplexMaxInterval instead.
   */
  private def getSimpleMaxInterval(subexpressions: Array[String], timeZone: Option[TimeZone]): Long = {
    val cronContainers = getCronContainers(subexpressions.take(3), timeZone) // seconds, minutes, hours
    val outermostContainer = cronContainers.findLast(!_.areAllUnitsMarked)
    outermostContainer.fold(1L) { outermost =>
      val innerContainers = cronContainers.takeWhile(_.unitType != outermost.unitType)
      val innerIntervalSeconds = innerContainers.map(_.wrapIntervalInSeconds).sum
      outermost.maxIntervalInSeconds + innerIntervalSeconds
    }
  }

  /**
   * Estimates the max interval using a combination of deduction (for the seconds and minutes), and sampling (for
   * everything else). We remove the seconds and minutes from the cron expression that is used for sampling so we can
   * effectively decrease the number of samples needed for a good estimate.
   */
  private def getComplexMaxInterval(subexpressions: Array[String], timeZone: Option[TimeZone]): Long = {
    val (secondsAndMinutes, everythingElse) = subexpressions.splitAt(2)

    // set up the dummy trigger
    val simplifiedComplexCron = everythingElse.mkString(s"0 0 ", " ", "")
    val selectedTimeZone = timeZone.getOrElse(TimeZone.getDefault)
    val cronSchedule = CronScheduleBuilder.cronSchedule(simplifiedComplexCron).inTimeZone(selectedTimeZone)
    val dummyTrigger: CronTrigger = TriggerBuilder.newTrigger().withSchedule(cronSchedule).build()
    val initialFireTime = Option(dummyTrigger.getFireTimeAfter(new Date()))

    // get the interval
    initialFireTime.fold(Long.MaxValue) { initialFireTime =>
      logger.debug(s"sample cron expression: $simplifiedComplexCron")
      val sampledMaxIntervalInSeconds = getSampledMaxInterval(initialFireTime, NUM_COMPLEX_SAMPLES, dummyTrigger)
      val innerMaxIntervalSeconds = getCronContainers(secondsAndMinutes, timeZone).map(_.wrapIntervalInSeconds).sum
      // subtract an hour to avoid double counting innerMaxIntervalSeconds
      sampledMaxIntervalInSeconds - HourUnitType.secondsPerUnit + innerMaxIntervalSeconds
    }
  }

  /**
   * Estimates the max interval for a given cron expression. The more samples, the better the estimate.
   */
  private def getSampledMaxInterval(prev: Date, numSamples: Long, trigger: CronTrigger, maxInterval: Long = 0): Long = {
    Option(trigger.getFireTimeAfter(prev)) match {
      case Some(next) if numSamples > 0 =>
        val intervalInSeconds = next.toInstant.getEpochSecond - prev.toInstant.getEpochSecond
        if (intervalInSeconds > maxInterval) {
          val sampleId = NUM_COMPLEX_SAMPLES - numSamples
          logger.debug(s"Seconds:$intervalInSeconds Sample:$sampleId Interval:$prev -> $next")
        }
        getSampledMaxInterval(next, numSamples - 1, trigger, Math.max(intervalInSeconds, maxInterval))
      case _ => maxInterval
    }
  }

  private def getCronContainers(parts: Array[String], timeZone: Option[TimeZone]): List[CronContainer] = {
    parts
      .zip(List(SecondUnitType, MinuteUnitType, HourUnitType))
      .map { case (str, cronType) => CronContainer(str, cronType, timeZone) }
      .toList
  }

}

sealed abstract class UnitType(val secondsPerUnit: Long, val numUnitsInContainer: Long)
case object SecondUnitType extends UnitType(1, 60)
case object MinuteUnitType extends UnitType(60, 60)
case object HourUnitType extends UnitType(3600, 24)

/**
 * A cron container is composed of "units". The "unitType" determines how many units are in the container, and how many
 * seconds are in each unit. "Marked units" describe when the trigger is set to fire. "Intervals" describe the number of
 * units between (but not including) marked units.
 */
case class CronContainer(str: String, unitType: UnitType, markedUnits: List[Long], timeZone: TimeZone) {
  lazy val areAllUnitsMarked: Boolean = markedUnits.size == unitType.numUnitsInContainer

  // the "wrap interval" is the interval that wraps around both ends of the container
  private lazy val wrapInterval = (unitType.numUnitsInContainer - markedUnits.last) + markedUnits.head
  lazy val wrapIntervalInSeconds: Long = unitsToSeconds(wrapInterval)

  // the max interval is the longest interval between any two marked units in the container, including the wrap interval
  lazy val maxIntervalInSeconds: Long = {
    val otherIntervals = markedUnits.zipWithIndex
      .take(markedUnits.size - 1)
      .map { case (value, index) => markedUnits(index + 1) - value }
    val units = (otherIntervals :+ wrapInterval).max
    val unitsWithDaylightSavings = units + (if (unitType == HourUnitType) getDaylightSavings(units, markedUnits) else 0)
    unitsToSeconds(unitsWithDaylightSavings)
  }

  /**
   * We multiply the number of units by the number of seconds in a single unit. We also subtract 1 unit to avoid double
   * counting the seconds in sub-intervals that are within a single unit. For example:
   *   - We subtract 1 hour when counting hours because the minutes make up the last hour
   *   - We subtract 1 minute when counting minutes because the seconds make up the last minute
   *   - We don't subtract 1 second when counting seconds because there are no smaller units
   */
  private def unitsToSeconds(units: Long): Long = {
    if (unitType == SecondUnitType) units else (units - 1) * unitType.secondsPerUnit
  }

  /**
   * Returns the number of seconds to add to the interval if daylight savings is being observed.
   */
  private def getDaylightSavings(currentNumUnits: Long, hourInstants: List[Long]): Long = {
    if (timeZone.observesDaylightTime()) {
      val extraSpringForwardHours = if (hourInstants.contains(2)) {
        val allBut2am = hourInstants.filter(_ != 2) // when springing forward, 2am is skipped for a day
        if (allBut2am.isEmpty) {
          23 // if we only trigger at 2am and 2am is skipped, we won't trigger for another 23 hours
        } else {
          // calculate the unique wrap interval when we spring forward
          // (number of hours remaining in the day, plus the number of hours until the first trigger in the next day)
          (24 - allBut2am.last) + (allBut2am.head - 1) - currentNumUnits
        }
      } else -1
      val extraFallbackHours = 1 // we always gain an extra hour when falling back (1am is delayed until 2am)
      Math.max(extraFallbackHours, extraSpringForwardHours)
    } else 0
  }

}

object CronContainer {
  def apply(str: String, cronType: UnitType, timeZone: Option[TimeZone]): CronContainer = {
    val markedUnits = CronContainer.getMarkedUnits(str, cronType.numUnitsInContainer)
    CronContainer(str, cronType, markedUnits, timeZone.getOrElse(TimeZone.getDefault))
  }

  def getMarkedUnits(str: String, unitsInContainer: Long): List[Long] = {
    val slash = """(\d{1,2}|\*)/(\d{1,2})""".r
    val dash = """(\d{1,2})-(\d{1,2})""".r
    str match {
      case "*" => (for (i <- 0L until unitsInContainer) yield i).toList
      case slash(start, interval) => {
        val startInt = if (start == "*") 0 else start.toLong
        getMarkedUnitsForSlash(startInt, interval.toLong, unitsInContainer, List(startInt))
      }
      case dash(first, second) =>
        val smallest = Math.min(first.toLong, second.toLong)
        val largest = Math.max(first.toLong, second.toLong)
        (for (i <- smallest to largest) yield i).toList
      case _ => str.split(",").map(_.toLong).toList.sorted
    }
  }

  private def getMarkedUnitsForSlash(
    start: Long,
    interval: Long,
    unitsInContainer: Long,
    result: List[Long] = Nil,
  ): List[Long] = {
    if (start + interval >= unitsInContainer) result
    else getMarkedUnitsForSlash(start + interval, interval, unitsInContainer, result :+ (start + interval))
  }
}
