package com.lucidchart.piezo.admin.utils

import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{Instant, LocalDate, Month, ZoneOffset}
import java.util.{Date, TimeZone}
import org.quartz.CronExpression
import play.api.Logging
import scala.annotation.tailrec
import scala.util.control.NonFatal

object CronHelper extends Logging {
  val IMPOSSIBLE_MAX_INTERVAL: Long = Long.MaxValue
  val DEFAULT_MAX_INTERVAL = 0
  val NON_EXISTENT: Int = -1

  /**
   * Approximates the largest interval between two trigger events for a given cron expression. This is a difficult
   * problem to solve perfectly, so this represents a "best effort approach" - the goal is to handle the most
   * expressions with the least amount of complexity.
   *
   * Known limitations:
   *   1. Daylight savings
   *   1. Complex year subexpressions
   * @param cronExpression
   */
  def getMaxInterval(cronExpression: String): Long = {
    try {
      val (secondsMinutesHourStrings, dayStrings) = cronExpression.split("\\s+").splitAt(3)
      val subexpressions = getSubexpressions(secondsMinutesHourStrings :+ dayStrings.mkString(" ")).reverse

      // find the largest subexpression that is not continuously triggering (*)
      val outermostIndex = subexpressions.indexWhere(!_.isContinuouslyTriggering)
      if (outermostIndex == NON_EXISTENT) 1
      else {
        // get the max interval for this expression
        val outermost = subexpressions(outermostIndex)
        if (outermost.maxInterval == IMPOSSIBLE_MAX_INTERVAL) IMPOSSIBLE_MAX_INTERVAL
        else {
          // subtract the inner intervals of the smaller, nested subexpressions
          val nested = subexpressions.slice(outermostIndex + 1, subexpressions.size)
          val innerIntervalsOfNested = nested.collect { case expr: BoundSubexpression => expr.innerInterval }.sum
          outermost.maxInterval - innerIntervalsOfNested
        }
      }

    } catch {
      case NonFatal(e) =>
        logger.error("Failed to validate cron expression", e)
        DEFAULT_MAX_INTERVAL
    }
  }

  private def getSubexpressions(parts: Array[String]): IndexedSeq[Subexpression] = {
    parts
      .zip(List(Seconds, Minutes, Hours, Days))
      .map { case (str, cronType) => cronType(str) }
      .toIndexedSeq
  }
}

case class Seconds(str: String) extends BoundSubexpression(str, x => s"$x * * ? * *", ChronoUnit.SECONDS, 60)
case class Minutes(str: String) extends BoundSubexpression(str, x => s"0 $x * ? * *", ChronoUnit.MINUTES, 60)
case class Hours(str: String) extends BoundSubexpression(str, x => s"0 0 $x ? * *", ChronoUnit.HOURS, 24)
case class Days(str: String) extends UnboundSubexpression(str, x => s"0 0 0 $x", 400)

abstract class Subexpression(str: String, getSimplifiedCron: String => String) {
  def maxInterval: Long
  def isContinuouslyTriggering: Boolean

  protected def startDate: Date
  final protected lazy val cron: CronExpression = {
    val newCron = new CronExpression(getSimplifiedCron(str))
    newCron.setTimeZone(TimeZone.getTimeZone("UTC")) // use a timezone without daylight savings
    newCron
  }
}

/**
 * Represents a subexpression in which the range over which the triggers occur is bound or fixed. For example, seconds
 * always occur within a minute, minutes always occur within an hour, and hours always occur within a day. Because the
 * range is fixed, we can determine all possibilities by sampling over the entire range.
 */
abstract class BoundSubexpression(
  str: String,
  getSimplifiedCron: String => String,
  temporalUnit: TemporalUnit,
  val numUnitsInContainer: Long,
) extends Subexpression(str, getSimplifiedCron) {

  final override protected val startDate = new Date(BoundSubexpression.startInstant.toEpochMilli)
  final protected val endDate = Date.from(
    BoundSubexpression.startInstant.plus(numUnitsInContainer, temporalUnit),
  )
  final override lazy val maxInterval: Long = getMaxInterval(cron, startDate, endDate, 0)
  final override lazy val isContinuouslyTriggering: Boolean = maxInterval == temporalUnit.getDuration.getSeconds

  /**
   * The interval between the first and last trigger within the range, or "everything but the ends". Should encompass
   * every trigger produced by the subexpression.
   */
  final lazy val innerInterval: Long = getInnerInterval(cron, startDate, endDate)

  @tailrec
  private def getMaxInterval(expr: CronExpression, prev: Date, end: Date, maxInterval: Long): Long = {
    Option(expr.getTimeAfter(prev)) match {
      case Some(curr) if !prev.after(end) => // iterate once past the "end" in order to wrap around
        val currentInterval = (curr.getTime - prev.getTime) / 1000
        val newMax = Math.max(currentInterval, maxInterval)
        getMaxInterval(expr, curr, end, newMax)
      case _ => maxInterval
    }
  }

  private def getInnerInterval(expr: CronExpression, prev: Date, end: Date): Long = {
    Option(expr.getTimeAfter(prev)).fold(Long.MaxValue) { firstTriggerDate =>
      val firstTriggerTime = firstTriggerDate.getTime / 1000
      val lastTriggerTime = getLastTriggerTime(expr, firstTriggerDate, end)
      lastTriggerTime - firstTriggerTime
    }
  }

  @tailrec
  private def getLastTriggerTime(expr: CronExpression, prev: Date, end: Date): Long = {
    Option(expr.getTimeAfter(prev)) match { // stop iterating before going past the "end"
      case Some(curr) if !curr.after(end) => getLastTriggerTime(expr, curr, end)
      case _                              => prev.getTime / 1000
    }
  }
}

object BoundSubexpression {
  final protected val startInstant: Instant = LocalDate
    .of(2010, Month.SEPTEMBER, 3)
    .atStartOfDay
    .toInstant(ZoneOffset.UTC)
    .minus(1, ChronoUnit.SECONDS)
}

/**
 * Represents a subexpression that is unbound, meaning that the range over which triggers occur is unknown, or is
 * variable. For example, days can occur within a week, month, or year, and each of these ranges can vary in size.
 * Because we can't determine the range over which days are triggered, we estimate the max interval by sampling a
 * certain number of times. The larger the number of samples, the more accurate the estimate.
 */
abstract class UnboundSubexpression(
  str: String,
  getSimplifiedCron: String => String,
  val maxNumSamples: Long,
) extends Subexpression(str, getSimplifiedCron)
    with Logging {

  final override protected val startDate = new Date
  final override lazy val maxInterval: Long = getSampledMaxInterval(startDate, maxNumSamples, cron)
  final override lazy val isContinuouslyTriggering: Boolean = str.split(" ").forall(expr => expr == "*" || expr == "?")

  @tailrec
  private def getSampledMaxInterval(prev: Date, numSamples: Long, expr: CronExpression, maxInterval: Long = 0): Long = {
    Option(expr.getTimeAfter(prev)) match {
      case Some(next) if numSamples > 0 =>
        val intervalInSeconds = (next.getTime - prev.getTime) / 1000
        if (intervalInSeconds > maxInterval) {
          val sampleId = maxNumSamples - numSamples
          logger.debug(s"Seconds:$intervalInSeconds Sample:$sampleId Interval:$prev -> $next")
        }
        getSampledMaxInterval(next, numSamples - 1, expr, Math.max(intervalInSeconds, maxInterval))
      case _ => if (prev.equals(startDate)) CronHelper.IMPOSSIBLE_MAX_INTERVAL else maxInterval
    }
  }
}
