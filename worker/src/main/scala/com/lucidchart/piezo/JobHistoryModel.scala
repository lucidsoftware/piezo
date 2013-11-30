package com.lucidchart.piezo

import org.quartz.JobExecutionContext
import java.sql.Timestamp
import org.slf4j.LoggerFactory
import java.util.{Properties, Date}

case class JobRecord(
name:String,
group:String,
trigger_name:String,
trigger_group:String,
success:Int,
start:Date,
finish:Date
)

class JobHistoryModel(props: Properties) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val connectionProvider = new ConnectionProvider(props)

  def addJob(context: JobExecutionContext, success:Boolean) {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""INSERT INTO job_history(fire_instance_id, job_name, job_group, trigger_name, trigger_group, success, start, finish) VALUES(?, ?, ?, ?, ?, ?, ?, ?)""")
      prepared.setString(1, context.getFireInstanceId)
      prepared.setString(2, context.getTrigger.getJobKey.getName)
      prepared.setString(3, context.getTrigger.getJobKey.getGroup)
      prepared.setString(4, context.getTrigger.getKey.getName)
      prepared.setString(5, context.getTrigger.getKey.getGroup)
      prepared.setBoolean(6, success)
      prepared.setTimestamp(7, new Timestamp(context.getFireTime.getTime))
      prepared.setTimestamp(8, new Timestamp(context.getFireTime.getTime + context.getJobRunTime))
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
      val prepared = connection.prepareStatement("""DELETE FROM job_history WHERE start < ?""")
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

  def getJob(name: String, group: String): List[JobRecord] = {
    val connection = connectionProvider.getConnection

    try {
      val prepared = connection.prepareStatement("""SELECT * FROM job_history WHERE job_name=? AND job_group=? ORDER BY start DESC LIMIT 100""")
      prepared.setString(1, name)
      prepared.setString(2, group)
      val rs = prepared.executeQuery();

      var result = List[JobRecord]()
      while(rs.next()) {
        result :+= new JobRecord(
          rs.getString("job_name"),
          rs.getString("job_group"),
          rs.getString("trigger_name"),
          rs.getString("trigger_group"),
          rs.getInt("success"),
          rs.getTimestamp("start"),
          rs.getTimestamp("finish")
        )
      }
      result
    } catch {
      case e: Exception => logger.error("error in retrieving jobs", e)
      List()
    } finally {
      connection.close()
    }
  }
}
