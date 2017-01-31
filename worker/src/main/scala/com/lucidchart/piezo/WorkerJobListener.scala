package com.lucidchart.piezo

import com.timgroup.statsd.StatsDClient
import java.util.Properties
import org.quartz.{JobExecutionContext, JobExecutionException, JobListener}
import org.slf4j.LoggerFactory;

object WorkerJobListener {
  val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerJobListener(props: Properties, statsd: StatsDClient) extends JobListener {
  val jobHistoryModel = new JobHistoryModel(props)
  val triggerMonitoringPriorityModel = new TriggerMonitoringModel(props)
  def getName: String = "WorkerJobListener"

  def jobToBeExecuted(context: JobExecutionContext) {}

  def jobExecutionVetoed(context: JobExecutionContext) {}

  def jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException) {
    try {
      val success = jobException == null
      jobHistoryModel.addJob(context,success = success)

      val suffix = if (success) ".succeeded" else ".failed"
      statsd.increment(s"job${suffix}")
      val statsKey = "jobs." + context.getTrigger.getJobKey.getGroup + "." + context.getTrigger.getJobKey.getName + suffix

      if (props.getProperty("com.lucidchart.piezo.enableMonitoring") == "new") {
        triggerMonitoringPriorityModel.getTriggerMonitoringRecord(context.getTrigger).map { triggerMonitoringRecord =>
          if (triggerMonitoringRecord.priority > TriggerMonitoringPriority.Off) {
            statsd.increment(statsKey)
          }
        }
      } else {
        statsd.increment(statsKey)
      }
    } catch {
      case e: Exception => WorkerJobListener.logger.error("error in jobWasExecuted", e)
    }
  }
}