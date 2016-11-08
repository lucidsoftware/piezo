package com.lucidchart.piezo

import org.quartz.{JobKey, Trigger}
import java.sql.Timestamp

import org.slf4j.LoggerFactory
import java.util.{Date, Properties}

case class TriggerRecord(
  name: String,
  group: String,
  scheduled_start: Date,
  actual_start: Date,
  finish: Date,
  misfire: Int,
  fire_instance_id: String
)

class TriggerHistoryModel(props: Properties) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val connectionProvider = new ConnectionProvider(props)

  def addTrigger(trigger: Trigger, actualStart: Option[Date], misfire: Boolean, fireInstanceId: Option[String]) {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""INSERT INTO trigger_history(trigger_name, trigger_group, scheduled_start, actual_start, finish, misfire, fire_instance_id) VALUES(?, ?, ?, ?, ?, ?, ?)""")
      prepared.setString(1, trigger.getKey.getName)
      prepared.setString(2, trigger.getKey.getGroup)
      prepared.setTimestamp(3, new Timestamp(Option(trigger.getPreviousFireTime).getOrElse(new Date(0)).getTime))
      prepared.setTimestamp(4, actualStart.map(date => new Timestamp(date.getTime)).getOrElse(null))
      prepared.setTimestamp(5, new Timestamp(System.currentTimeMillis))
      prepared.setBoolean(6, misfire)
      prepared.setString(7, fireInstanceId.getOrElse(""))
      prepared.executeUpdate()
    } catch {
      case e:Exception => logger.error("error in recording end of trigger",e)
    } finally {
      connection.close()
    }
  }

  def deleteTriggers(minScheduledStart: Long): Int = {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""DELETE FROM trigger_history WHERE scheduled_start < ?""")
      prepared.setTimestamp(1, new Timestamp(minScheduledStart))
      prepared.executeUpdate()
    } catch {
      case e:Exception =>
        logger.error("error deleting trigger histories",e)
        0
    } finally {
      connection.close()
    }
  }

  def getTrigger(name: String, group: String): List[TriggerRecord] = {
    val connection = connectionProvider.getConnection

    try {
      val prepared = connection.prepareStatement("""SELECT * FROM trigger_history WHERE trigger_name=? AND trigger_group=? ORDER BY scheduled_start DESC LIMIT 100""")
      prepared.setString(1, name)
      prepared.setString(2, group)
      val rs = prepared.executeQuery()

      var result = List[TriggerRecord]()
      while(rs.next()) {
        result :+= new TriggerRecord(
          rs.getString("trigger_name"),
          rs.getString("trigger_group"),
          rs.getTimestamp("scheduled_start"),
          rs.getTimestamp("actual_start"),
          rs.getTimestamp("finish"),
          rs.getInt("misfire"),
          rs.getString("fire_instance_id")
        )
      }
      result
    } catch {
      case e: Exception => logger.error("error in retrieving triggers", e)
      List()
    } finally {
      connection.close()
    }
  }
}

