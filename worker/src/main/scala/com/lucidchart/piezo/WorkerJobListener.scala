package com.lucidchart.piezo

import com.timgroup.statsd.StatsDClient
import java.util.Properties
import org.quartz.{JobExecutionContext, JobExecutionException, JobListener}
import org.slf4j.LoggerFactory
import scala.util.Try;

object WorkerJobListener {
  val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerJobListener(props: Properties, statsd: StatsDClient, useDatadog: Boolean) extends JobListener {
  val jobHistoryModel = new JobHistoryModel(props)

  def getName: String = "WorkerJobListener"

  def jobToBeExecuted(context: JobExecutionContext) {}

  def jobExecutionVetoed(context: JobExecutionContext) {}

  def jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException) {
    try {
      val success = jobException == null
      jobHistoryModel.addJob(context, success = success)

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