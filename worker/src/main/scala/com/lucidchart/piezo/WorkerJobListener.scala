package com.lucidchart.piezo

import com.timgroup.statsd.StatsDClient
import java.util.Properties
import org.quartz.{JobExecutionContext, JobExecutionException, JobListener}
import org.slf4j.LoggerFactory
import org.slf4j.Logger

object WorkerJobListener {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerJobListener(props: Properties, statsd: StatsDClient, useDatadog: Boolean) extends JobListener {
  val jobHistoryModel = new JobHistoryModel(props)

  def getName: String = "WorkerJobListener"

  def jobToBeExecuted(context: JobExecutionContext): Unit = {}

  def jobExecutionVetoed(context: JobExecutionContext): Unit = {}

  def jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException): Unit = {
    try {
      val success = jobException == null
      jobHistoryModel.addJob(
        context.getFireInstanceId,
        context.getTrigger.getJobKey,
        context.getTrigger.getKey,
        context.getFireTime,
        context.getJobRunTime,
        success = success,
      )

      val suffix = if (success) "succeeded" else "failed"
      val jobKey = s"${context.getTrigger.getJobKey.getGroup}.${context.getTrigger.getJobKey.getName}"
      if (useDatadog) {
        statsd.increment("jobs", s"job:${jobKey}", s"event:${suffix}")
      } else {
        statsd.increment(s"jobs.${jobKey}.${suffix}")
      }
    } catch {
      case e: Exception => WorkerJobListener.logger.error("error in jobWasExecuted", e)
    }
  }
}
