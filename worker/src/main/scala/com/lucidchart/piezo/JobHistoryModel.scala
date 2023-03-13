package com.lucidchart.piezo

import java.sql.{ResultSet, Timestamp}
import java.util.{Date, Properties}
import org.quartz.{JobKey, TriggerKey}
import org.slf4j.LoggerFactory

case class JobRecord(
  name: String,
  group: String,
  trigger_name: String,
  trigger_group: String,
  success: Int,
  start: Date,
  finish: Date,
  fire_instance_id: String
)

class JobHistoryModel(props: Properties) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val connectionProvider = new ConnectionProvider(props)

  def addJob(
    fireInstanceId: String,
    jobKey: JobKey,
    triggerKey: TriggerKey,
    fireTime: Date,
    instanceDurationInMillis: Long,
    success:Boolean
  ): Unit = {
    val connection = connectionProvider.getConnection
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
        """.stripMargin
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
      case e:Exception => logger.error("error in recording start of job",e)
    } finally {
      connection.close()
    } //TODO: close statement?
  }

  def deleteJobs(minStart: Long): Int = {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement(
        """
          DELETE
          FROM job_history
          WHERE start < ?
        """.stripMargin
      )
      prepared.setTimestamp(1, new Timestamp(minStart))
      prepared.executeUpdate()
    } catch {
      case e:Exception =>
        logger.error("error deleting job histories",e)
        0
    } finally {
      connection.close()
    }
  }

  def getJob(jobKey: JobKey): List[JobRecord] = {
    val connection = connectionProvider.getConnection

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
        """.stripMargin
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
    val connection = connectionProvider.getConnection

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
        """.stripMargin
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
    val connection = connectionProvider.getConnection

    try {
      val prepared = connection.prepareStatement(
        """
          SELECT *
          FROM job_history
          ORDER BY start DESC
          LIMIT 100
        """.stripMargin
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
    while(rs.next()) {
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
      rs.getString("fire_instance_id")
    )
  }
}
