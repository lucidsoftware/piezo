package com.lucidchart.piezo

import java.sql.{ResultSet, Timestamp}
import java.util.Date
import org.quartz.{JobDataMap, JobKey, TriggerKey}
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import java.sql.Connection
import java.time.Instant

case class JobRecord(
  name: String,
  group: String,
  trigger_name: String,
  trigger_group: String,
  success: Int,
  start: Date,
  finish: Date,
  fire_instance_id: String,
)

class JobHistoryModel(getConnection: () => Connection) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // Trigger Group for records that aren't deletable
  private final val oneTimeJobTriggerGroup = "ONE_TIME_JOB"
  final def oneTimeTriggerKey(fireInstanceId: Long): TriggerKey =
    TriggerKey(fireInstanceId.toString, oneTimeJobTriggerGroup)

  // Methods to store the one-time-job id in a job-data-map
  final val jobDataMapOneTimeJobKey = "OneTimeJobId"
  final def getOneTimeJobIdFromDataMap(jobDataMap: JobDataMap): Option[String] = Option(
    jobDataMap.getString(jobDataMapOneTimeJobKey),
  )
  final def createJobDataMapForOneTimeJob(id: String): JobDataMap = new JobDataMap(
    java.util.Map.of(jobDataMapOneTimeJobKey, id),
  )

  def addJob(
    fireInstanceId: String,
    jobKey: JobKey,
    triggerKey: TriggerKey,
    fireTime: Date,
    instanceDurationInMillis: Long,
    success: Boolean,
  ): Unit = {
    val connection = getConnection()
    try {
      val prepared = connection.prepareStatement(
        """
          INSERT INTO job_history(
            fire_instance_id,
            job_name,
            job_group,
            trigger_name,
            trigger_group,
            success,
            start,
            finish
          )
          VALUES(?, ?, ?, ?, ?, ?, ?, ?)
        """.stripMargin,
      )
      prepared.setString(1, fireInstanceId)
      prepared.setString(2, jobKey.getName)
      prepared.setString(3, jobKey.getGroup)
      prepared.setString(4, triggerKey.getName)
      prepared.setString(5, triggerKey.getGroup)
      prepared.setBoolean(6, success)
      prepared.setTimestamp(7, new Timestamp(fireTime.getTime))
      prepared.setTimestamp(8, new Timestamp(fireTime.getTime + instanceDurationInMillis))
      prepared.executeUpdate()
    } catch {
      case e: Exception => logger.error("error in recording start of job", e)
    } finally {
      connection.close()
    } // TODO: close statement?
  }

  def deleteJobs(minStart: Long): Int = {
    val connection = getConnection()
    try {
      val prepared = connection.prepareStatement(
        s"""
          DELETE
          FROM job_history
          WHERE start < ?
            AND trigger_group != '$oneTimeJobTriggerGroup'
        """.stripMargin,
      )
      prepared.setTimestamp(1, new Timestamp(minStart))
      prepared.executeUpdate()
    } catch {
      case e: Exception =>
        logger.error("error deleting job histories", e)
        0
    } finally {
      connection.close()
    }
  }

  def getJob(jobKey: JobKey): List[JobRecord] = {
    val connection = getConnection()

    try {
      val prepared = connection.prepareStatement(
        """
          SELECT *
          FROM job_history
          WHERE
            job_name=?
            AND job_group=?
          ORDER BY start DESC
          LIMIT 100
        """.stripMargin,
      )
      prepared.setString(1, jobKey.getName)
      prepared.setString(2, jobKey.getGroup)
      val rs = prepared.executeQuery();
      parseJobs(rs)
    } catch {
      case e: Exception => {
        logger.error("error in retrieving jobs", e)
        Nil
      }
    } finally {
      connection.close()
    }
  }

  def getLastJobSuccessByTrigger(triggerKey: TriggerKey): Option[JobRecord] = {
    val connection = getConnection()

    try {
      val prepared = connection.prepareStatement(
        """
          SELECT *
          FROM job_history
          WHERE
            trigger_name=?
            AND trigger_group=?
            AND success=1
          ORDER BY start DESC
          LIMIT 1
        """.stripMargin,
      )
      prepared.setString(1, triggerKey.getName)
      prepared.setString(2, triggerKey.getGroup)
      val rs = prepared.executeQuery()
      if (rs.next()) {
        Some(parseJob(rs))
      } else {
        None
      }
    } catch {
      case e: Exception => {
        logger.error("error in retrieving last job success by trigger", e)
        None
      }
    } finally {
      connection.close()
    }
  }

  def getJobs(): List[JobRecord] = {
    val connection = getConnection()

    try {
      val prepared = connection.prepareStatement(
        """
          SELECT *
          FROM job_history
          ORDER BY start DESC
          LIMIT 100
        """.stripMargin,
      )
      val rs = prepared.executeQuery()
      parseJobs(rs)
    } catch {
      case e: Exception => {
        logger.error("error in retrieving jobs", e)
        Nil
      }
    } finally {
      connection.close()
    }
  }

  def parseJobs(rs: ResultSet): List[JobRecord] = {
    var result = List[JobRecord]()
    while (rs.next()) {
      result :+= parseJob(rs)
    }
    result
  }

  def parseJob(rs: ResultSet): JobRecord = {
    new JobRecord(
      rs.getString("job_name"),
      rs.getString("job_group"),
      rs.getString("trigger_name"),
      rs.getString("trigger_group"),
      rs.getInt("success"),
      rs.getTimestamp("start"),
      rs.getTimestamp("finish"),
      rs.getString("fire_instance_id"),
    )
  }

  /**
   * Check if we have already triggered a one-time-job with the given trigger key and fireInstanceId.
   *
   * This is useful for seeing if a one-time job has already been triggered, to ensure that triggering a one-time job
   * with the same instance id is an idempotent operation. If the one-time job has not been triggered, the same
   * transaction is used to add the one-time-job to the database, to avoid race conditions
   */
  def addOneTimeJobIfNotExists(jobKey: JobKey, fireInstanceId: Long): Boolean = {
    val connection = getConnection()

    // Use a trigger key that the database won't clean up in "JobHistoryCleanup"
    val triggerKey: TriggerKey = oneTimeTriggerKey(fireInstanceId)

    try {
      // Mark this trigger (as already run) in the same transaction, to prevent race conditions where two requests
      // trigger the same job
      val triggerStartTime: Instant = Instant.now()
      // Same as "addJob()" but without the finish
      val prepared = connection.prepareStatement(
        """
            INSERT IGNORE INTO job_history(
              fire_instance_id,
              job_name,
              job_group,
              trigger_name,
              trigger_group,
              success,
              start
            )
            VALUES(?, ?, ?, ?, ?, ?, ?)
          """.stripMargin,
      )
      prepared.setString(1, fireInstanceId.toString)
      prepared.setString(2, jobKey.getName)
      prepared.setString(3, jobKey.getGroup)
      prepared.setString(4, triggerKey.getName)
      prepared.setString(5, triggerKey.getGroup)
      prepared.setBoolean(6, true)
      prepared.setObject(7, triggerStartTime)
      // Check if we actually inserted the row,
      // if we didn't, then the firs_instance_id was already in use
      prepared.executeUpdate() > 0
    } finally {
      connection.close()
    }
  }

  def completeOneTimeJob(
    fireInstanceId: String,
    fireTime: Instant,
    instanceDurationInMillis: Long,
    success: Boolean,
  ): Unit = {
    val connection = getConnection()
    try {
      val prepared = connection.prepareStatement(
        """
          UPDATE job_history
          SET
            success=?,
            start=?,
            finish=?
          WHERE fire_instance_id=?
        """.stripMargin,
      )
      prepared.setBoolean(1, success)
      prepared.setObject(2, fireTime)
      prepared.setObject(3, fireTime.plusMillis(instanceDurationInMillis))
      prepared.setString(4, fireInstanceId)
      prepared.executeUpdate()
    } catch {
      case e: Exception => logger.error("error in recording completion of one-time-job", e)
    } finally {
      connection.close()
    }
  }
}
