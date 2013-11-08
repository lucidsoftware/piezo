package com.lucidchart.piezo

import org.quartz.JobExecutionContext
import java.sql.Timestamp
import org.slf4j.LoggerFactory
import java.util.Properties

class JobHistoryModel(props: Properties) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val pwd = props.getProperty("org.quartz.dataSource.jobs.password")
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
    }
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
}
