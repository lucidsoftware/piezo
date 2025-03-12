package com.lucidchart.piezo.admin.models
import org.quartz.Trigger

object TriggerType extends Enumeration {
  type TriggerType = Value
  val Cron: Value = Value(0, "cron")
  val Simple: Value = Value(1, "simple")
  val Unknown: Value = Value(2, "unknown")

  def apply(trigger: Trigger): Value = {
    if (trigger.isInstanceOf[org.quartz.CronTrigger]) {
      TriggerType.Cron
    } else if (trigger.isInstanceOf[org.quartz.SimpleTrigger]) {
      TriggerType.Simple
    } else {
      TriggerType.Unknown
    }
  }
}
