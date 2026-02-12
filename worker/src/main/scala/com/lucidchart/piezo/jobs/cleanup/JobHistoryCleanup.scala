package com.lucidchart.piezo.jobs.cleanup

import org.quartz.{Job, JobExecutionContext}
import org.slf4j.LoggerFactory
import com.lucidchart.piezo.{JobHistoryModel, TriggerHistoryModel, Worker}
import java.time.{Duration, Instant}
import java.sql.Connection

class JobHistoryCleanup extends Job {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def execute(context: JobExecutionContext): Unit = {
    val maxAge = Duration.ofDays(context.getMergedJobDataMap.getLong("maxAgeDays"))
    val minStart = Instant.now().minus(maxAge)
    val getConnection = Worker.connectionFactory(context.getScheduler.getContext)
    deleteTriggerHistories(getConnection, minStart)
    deleteJobHistories(getConnection, minStart)
  }

  private[this] def deleteTriggerHistories(getConnection: () => Connection, minStart: Instant): Unit = {
    logger.info("Deleting triggers older than " + minStart)
    val numDeleted = new TriggerHistoryModel(getConnection).deleteTriggers(minStart)
    logger.info("Deleted " + numDeleted + " trigger histories")
  }

  private[this] def deleteJobHistories(getConnection: () => Connection, minStart: Instant): Unit = {
    logger.info("Deleting jobs older than " + minStart)
    val numDeleted = new JobHistoryModel(getConnection).deleteJobs(minStart)
    logger.info("Deleted " + numDeleted + " job histories")
  }
}
