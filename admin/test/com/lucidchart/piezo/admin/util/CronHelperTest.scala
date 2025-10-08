package com.lucidchart.piezo.admin.util

import com.lucidchart.piezo.admin.utils.CronHelper
import org.specs2.mutable.Specification

class CronHelperTest extends Specification {

  val SECOND: Int = 1
  val MINUTE: Int = 60 * SECOND
  val HOUR: Int = 60 * MINUTE
  val DAY: Int = 24 * HOUR
  val WEEK: Int = 7 * DAY
  val YEAR: Int = 365 * DAY
  val LEAP_YEAR: Int = YEAR + DAY

  def maxInterval(str: String): Option[Int] = CronHelper.getMaxInterval(str)

  "CronHelper" should {
    "validate basic cron expressions" in {
      maxInterval("* * * * * ?") must beSome(SECOND) // every second
      maxInterval("0 * * * * ?") must beSome(MINUTE) // second 0 of every minute
      maxInterval("0 0 * * * ?") must beSome(HOUR) // second 0 during minute 0 of every hour
      maxInterval("0 0 0 * * ?") must beSome(DAY) // second 0 during minute 0 during hour 0 of every day
      maxInterval("* 0 * * * ?") must beSome(HOUR - MINUTE + SECOND) // every second during minute 0
      maxInterval("* * 0 * * ?") must beSome(DAY - HOUR + SECOND)
    }

    "validate more basic cron expressions" in {
      maxInterval("0/1 0-59 */1 * * ?") must beSome(SECOND) // variations on 1 second
      maxInterval("* * 0-23 * * ?") must beSome(SECOND)
      maxInterval("22 2/6 * * * ?") must beSome(6 * MINUTE) // 22nd second of every 6th minute after minute 2
      maxInterval("*/15 * * * * ?") must beSome(15 * SECOND)
      maxInterval("30 10 */1 * * ?") must beSome(HOUR)
      maxInterval("15 * * * * ?") must beSome(MINUTE)
      maxInterval("3,2,1,0 45,44,16,15 6,5,4 * * ? *") must beSome(21 * HOUR + 29 * MINUTE + 57 * SECOND)
      maxInterval("50-0 30-40 14-12 * * ?") must beSome(1 * HOUR + 49 * MINUTE + 1 * SECOND)
      maxInterval("0 0 8-4 * * ?") must beSome(4 * HOUR)
      maxInterval("0 0 0/6 * * ? *") must beSome(6 * HOUR)
      maxInterval("0 10,20,30 * * ? *") must beSome(40 * MINUTE)
      maxInterval("0-10/2 0-5,20-25 0,5-11/2,20-23 * ? *") must beSome(8 * HOUR + 34 * MINUTE + 50 * SECOND)
    }

    "validate complex cron expressions" in {
      maxInterval("0/15 * * 1-12 * ?") must beSome(19 * DAY + 15 * SECOND) // every 15 seconds on days 1-12 of the month
      maxInterval("* * * * 1-11 ?") must beSome(31 * DAY + SECOND) // every second of every month except for december
      maxInterval("* * * * * ? 1998") must beNone // every second of 1998
      maxInterval("0 0 20 ? 10 WED#2 2015") must beNone // a single moment in the past
      maxInterval("0 0 0 29 2 ? *") must beSome(
        8 * YEAR + DAY,
      ) // 8 years since we skip leap day roughly every 100 years
      maxInterval("* * * 29 2 ? *") must beSome(8 * YEAR + SECOND) // every second on leap day
      maxInterval("0 11 11 11 11 ?") must beSome(LEAP_YEAR) // every november 11th at 11:11am
      maxInterval("1 2 3 ? * 6") must beSome(WEEK) // every saturday
      maxInterval("0 15 10 ? * 6#3") must beSome(5 * WEEK) // third saturday of every month
      maxInterval("0 15 10 ? * MON-FRI") must beSome(3 * DAY) // every weekday
      maxInterval("0 0 0/6 * 1,2,3,4,5,6,7,8,9,10,11,12 ? *") must beSome(DAY - (18 * HOUR))
      maxInterval("* * * 1-31 * ?") must beSome(SECOND)
      maxInterval("* * * * 1-12 ?") must beSome(SECOND)
      maxInterval("* * * ? * 1-7") must beSome(SECOND)
    }
  }
}
