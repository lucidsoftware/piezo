package com.lucidchart.piezo

import com.timgroup.statsd.StatsDClient

import org.quartz.Trigger.{CompletedExecutionInstruction, TriggerState}
import org.quartz.*
import org.slf4j.LoggerFactory
import java.sql.Connection

object WorkerTriggerListener {
  private val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerTriggerListener(getConnection: () => Connection, statsd: StatsDClient, useDatadog: Boolean)
    extends TriggerListener {
  val triggerHistoryModel = new TriggerHistoryModel(getConnection)

  def getName: String = "WorkerTriggerListener"

  def vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean = {
    // Called right before the job is about to execute on the worker.

    /*
     Under certain conditions, a job may come up for execution after a trigger
      has been paused, leading to unexpected additional executions.

     To fix this, right before Quartz starts the job, we check the _current_ trigger
     state to ensure that job wasn't paused after the execution was queued.
     */

    // Veto if current trigger state is paused
    context.getScheduler.getTriggerState(trigger.getKey) == TriggerState.PAUSED
  }

  def triggerFired(trigger: Trigger, context: JobExecutionContext): Unit = {
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
    triggerInstructionCode: CompletedExecutionInstruction,
  ): Unit = {
    try {
      triggerHistoryModel.addTrigger(
        trigger.getKey,
        Option(trigger.getPreviousFireTime),
        Some(context.getFireTime),
        misfire = false,
        Some(context.getFireInstanceId),
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
      triggerHistoryModel.addTrigger(
        trigger.getKey,
        Option(trigger.getPreviousFireTime),
        None,
        misfire = true,
        None,
      )

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
