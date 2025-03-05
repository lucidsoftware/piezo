package com.lucidchart.piezo.jobs.cleanup

import org.quartz.{Job, JobExecutionContext}
import org.slf4j.LoggerFactory
import com.lucidchart.piezo.{JobHistoryModel, TriggerHistoryModel, Worker}
import java.util.Date
import java.sql.Connection

class JobHistoryCleanup extends Job {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def execute(context: JobExecutionContext): Unit = {
    val maxAge = context.getMergedJobDataMap.getLong("maxAgeDays") * 24L * 3600L * 1000L
    val minStart = System.currentTimeMillis() - maxAge
    val getConnection = Worker.connectionFactory(context.getScheduler.getContext)
    deleteTriggerHistories(getConnection, minStart)
    deleteJobHistories(getConnection, minStart)
  }

  private[this] def deleteTriggerHistories(getConnection: () => Connection, minStart: Long): Unit = {
    logger.info("Deleting triggers older than " + new Date(minStart))
    val numDeleted = new TriggerHistoryModel(getConnection).deleteTriggers(minStart)
    logger.info("Deleted " + numDeleted + " trigger histories")
  }

  private[this] def deleteJobHistories(getConnection: () => Connection, minStart: Long): Unit = {
    logger.info("Deleting jobs older than " + new Date(minStart))
    val numDeleted = new JobHistoryModel(getConnection).deleteJobs(minStart)
    logger.info("Deleted " + numDeleted + " job histories")
  }
}
