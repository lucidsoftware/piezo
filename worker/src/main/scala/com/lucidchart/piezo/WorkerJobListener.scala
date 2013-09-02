package com.lucidchart.piezo

import org.quartz.{JobExecutionException, JobExecutionContext, JobListener}
import org.slf4j.LoggerFactory
import java.util.Properties

object WorkerJobListener {
  val logger = LoggerFactory.getLogger(this.getClass)
}

class WorkerJobListener(props: Properties) extends JobListener {
  val jobHistoryModel = new JobHistoryModel(props)
  def getName: String = "WorkerJobListener"

  def jobToBeExecuted(context: JobExecutionContext) {}

  def jobExecutionVetoed(context: JobExecutionContext) {}

  def jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException) {
    try {
      jobHistoryModel.addJob(context, success=jobException == null)
      val statsKey = "jobs." + context.getTrigger.getJobKey.getGroup + "." + context.getTrigger.getJobKey.getName + ".executed"
      StatsD.increment(statsKey)
    } catch {
      case e: Exception => WorkerJobListener.logger.error("error in jobWasExecuted", e)
    }
  }
}