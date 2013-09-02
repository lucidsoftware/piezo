package com.lucidchart.piezo

import org.quartz._
import org.quartz.Trigger.CompletedExecutionInstruction
import org.slf4j.LoggerFactory
import java.util.Properties

object WorkerTriggerListener {
  private val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerTriggerListener(props: Properties) extends TriggerListener {
  val triggerHistoryModel = new TriggerHistoryModel(props)
  def getName:String = "WorkerTriggerListener"

  def vetoJobExecution(trigger: Trigger, context: JobExecutionContext):Boolean = false

  def triggerFired(trigger: Trigger, context: JobExecutionContext) {}

  def triggerComplete(trigger: Trigger, context: JobExecutionContext, triggerInstructionCode: CompletedExecutionInstruction) {
    try {
      triggerHistoryModel.addTrigger(trigger, Some(context.getFireTime), misfire=false)
      val statsKey = "triggers." + trigger.getKey.getGroup + "." + trigger.getKey.getName + ".completed"
      StatsD.increment(statsKey)
    } catch {
      case e: Exception => WorkerTriggerListener.logger.error("exception in triggerComplete", e)
    }
  }

  def triggerMisfired(trigger: Trigger) {
    try {
      triggerHistoryModel.addTrigger(trigger, None, misfire=true)
      val statsKey = "triggers." + trigger.getKey.getGroup + "." + trigger.getKey.getName + ".misfired"
      StatsD.increment(statsKey)
    } catch {
      case e: Exception => WorkerTriggerListener.logger.error("exception in triggerMisfired", e)
    }
  }
}
