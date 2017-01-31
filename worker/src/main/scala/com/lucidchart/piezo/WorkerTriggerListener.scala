package com.lucidchart.piezo

import com.timgroup.statsd.StatsDClient
import java.util.Properties
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz._
import org.slf4j.LoggerFactory
import scala.util.Try

object WorkerTriggerListener {
  private val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerTriggerListener(props: Properties, statsd: StatsDClient, useDatadog: Boolean) extends TriggerListener {
  val triggerHistoryModel = new TriggerHistoryModel(props)

  def getName: String = "WorkerTriggerListener"

  def vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean = false

  def triggerFired(trigger: Trigger, context: JobExecutionContext):  Unit = {
    val triggerKey = s"${trigger.getKey.getGroup}.${trigger.getKey.getName}"
    if (useDatadog) {
      statsd.increment("triggers", s"trigger:${triggerKey}", "event:fired")
    } else {
      statsd.increment(s"triggers.${triggerKey}.fired")
    }
  }

  def triggerComplete(
    trigger: Trigger,
    context: JobExecutionContext,
    triggerInstructionCode: CompletedExecutionInstruction
  ): Unit = {
    try {
      triggerHistoryModel.addTrigger(
        trigger,
        Some(context.getFireTime),
        misfire = false, Some(context.getFireInstanceId)
      )

      val triggerKey = s"${trigger.getKey.getGroup}.${trigger.getKey.getName}"
      if (useDatadog) {
        statsd.increment("triggers", s"trigger:${triggerKey}", "event:completed")
      } else {
        statsd.increment(s"triggers.${triggerKey}.completed")
      }
    } catch {
      case e: Exception => WorkerTriggerListener.logger.error("exception in triggerComplete", e)
    }
  }

  def triggerMisfired(trigger: Trigger): Unit = {
    try {
      triggerHistoryModel.addTrigger(trigger, None, misfire = true, None)

      val triggerKey = s"${trigger.getKey.getGroup}.${trigger.getKey.getName}"
      if (useDatadog) {
        statsd.increment("triggers", s"trigger:${triggerKey}", "event:misfired")
      } else {
        statsd.increment(s"triggers.${triggerKey}.misfired")
      }
    } catch {
      case e: Exception => WorkerTriggerListener.logger.error("exception in triggerMisfired", e)
    }
  }
}
