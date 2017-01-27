package com.lucidchart.piezo

import com.lucidchart.util.statsd.StatsD
import java.util.Properties
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz._
import org.slf4j.LoggerFactory

object WorkerTriggerListener {
  private val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerTriggerListener(props: Properties, statsd: StatsD) extends TriggerListener {
  val triggerHistoryModel = new TriggerHistoryModel(props)
  val triggerMonitoringPriorityModel = new TriggerMonitoringModel(props)
  def getName: String = "WorkerTriggerListener"

  def vetoJobExecution(trigger: Trigger, context: JobExecutionContext): Boolean = false

  def triggerFired(trigger: Trigger, context: JobExecutionContext):  Unit = {}

  def triggerComplete(
    trigger: Trigger,
    context: JobExecutionContext,
    triggerInstructionCode: CompletedExecutionInstruction
  ): Unit = {
    try {
      statsd.increment("trigger.complete")
      triggerHistoryModel.addTrigger(trigger, Some(context.getFireTime), misfire = false, Some(context.getFireInstanceId))
      val statsKey = "triggers." + trigger.getKey.getGroup + "." + trigger.getKey.getName + ".completed"

      if (props.getProperty("com.lucidchart.piezo.enableMonitoring") == "new") {
        triggerMonitoringPriorityModel.getTriggerMonitoringRecord(trigger).map { triggerMonitoringRecord =>
          if (triggerMonitoringRecord.priority > TriggerMonitoringPriority.Off) {
            statsd.increment(statsKey)
          }
        }
      } else {
        statsd.increment(statsKey)
      }
    } catch {
      case e: Exception => WorkerTriggerListener.logger.error("exception in triggerComplete", e)
    }
  }

  def triggerMisfired(trigger: Trigger): Unit = {
    try {
      statsd.increment("trigger.misfired")
      triggerMonitoringPriorityModel.getTriggerMonitoringRecord(trigger).map { triggerMonitoringRecord =>
        triggerHistoryModel.addTrigger(trigger, None, misfire = true, None)

        if (triggerMonitoringRecord.priority > TriggerMonitoringPriority.Off) {
          val statsKey = "triggers." + trigger.getKey.getGroup + "." + trigger.getKey.getName + ".misfired"
          statsd.increment(statsKey)
        }
      }
    } catch {
      case e: Exception => WorkerTriggerListener.logger.error("exception in triggerMisfired", e)
    }
  }
}
