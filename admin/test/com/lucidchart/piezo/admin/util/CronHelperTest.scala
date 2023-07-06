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
  val IMPOSSIBLE: Long = Long.MaxValue

  def maxInterval(str: String): Long = CronHelper.getMaxInterval(str)

  "CronHelper" should {
    "validate basic cron expressions" in {
      maxInterval("* * * * * ?") mustEqual SECOND // every second
      maxInterval("0 * * * * ?") mustEqual MINUTE // second 0 of every minute
      maxInterval("0 0 * * * ?") mustEqual HOUR // second 0 during minute 0 of every hour
      maxInterval("0 0 0 * * ?") mustEqual DAY // second 0 during minute 0 during hour 0 of every day
      maxInterval("* 0 * * * ?") mustEqual (HOUR - MINUTE + SECOND) // every second during minute 0
      maxInterval("* * 0 * * ?") mustEqual (DAY - HOUR + SECOND)
    }

    "validate more basic cron expressions" in {
      maxInterval("0/1 0-59 */1 * * ?") mustEqual SECOND // variations on 1 second
      maxInterval("* * 0-23 * * ?") mustEqual SECOND
      maxInterval("22 2/6 * * * ?") mustEqual 6 * MINUTE // 22nd second of every 6th minute after minute 2
      maxInterval("*/15 * * * * ?") mustEqual 15 * SECOND
      maxInterval("30 10 */1 * * ?") mustEqual HOUR
      maxInterval("15 * * * * ?") mustEqual MINUTE
      maxInterval("3,2,1,0 45,44,16,15 6,5,4 * * ? *") mustEqual (21 * HOUR + 29 * MINUTE + 57 * SECOND)
      maxInterval("50-0 30-40 14-12 * * ?") mustEqual (1 * HOUR + 49 * MINUTE + 1 * SECOND)
      maxInterval("0 0 8-4 * * ?") mustEqual 4 * HOUR
      maxInterval("0 0 0/6 * * ? *") mustEqual 6 * HOUR
      maxInterval("0 10,20,30 * * ? *") mustEqual 40 * MINUTE
      maxInterval("0-10/2 0-5,20-25 0,5-11/2,20-23 * ? *") mustEqual 8 * HOUR + 34 * MINUTE + 50 * SECOND
    }

    "validate complex cron expressions" in {
      maxInterval("0/15 * * 1-12 * ?") mustEqual 19 * DAY + 15 * SECOND // every 15 seconds on days 1-12 of the month
      maxInterval("* * * * 1-11 ?") mustEqual 31 * DAY + SECOND // every second of every month except for december
      maxInterval("* * * * * ? 1998") mustEqual IMPOSSIBLE // every second of 1998
      maxInterval("0 0 0 29 2 ? *") mustEqual 8 * YEAR + DAY // 8 years since we skip leap day roughly every 100 years
      maxInterval("* * * 29 2 ? *") mustEqual 8 * YEAR + SECOND // every second on leap day
      maxInterval("0 11 11 11 11 ?") mustEqual LEAP_YEAR // every november 11th at 11:11am
      maxInterval("1 2 3 ? * 6") mustEqual WEEK // every saturday
      maxInterval("0 15 10 ? * 6#3") mustEqual 5 * WEEK // third saturday of every month
      maxInterval("0 15 10 ? * MON-FRI") mustEqual 3 * DAY // every weekday
      maxInterval("0 0 0/6 * 1,2,3,4,5,6,7,8,9,10,11,12 ? *") mustEqual DAY - (18 * HOUR)
      maxInterval("* * * 1-31 * ?") mustEqual SECOND
      maxInterval("* * * * 1-12 ?") mustEqual SECOND
      maxInterval("* * * ? * 1-7") mustEqual SECOND
    }
  }
}
