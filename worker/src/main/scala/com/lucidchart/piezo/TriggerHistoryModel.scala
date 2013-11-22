package com.lucidchart.piezo

import org.quartz.Trigger
import java.sql.Timestamp
import org.slf4j.LoggerFactory
import java.util.{Properties, Date}

case class TriggerRecord(
  name:String,
  group:String,
  scheduled_start:Date,
  actual_start:Date,
  finish:Date,
  misfire:Int
)

class TriggerHistoryModel(props: Properties) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val connectionProvider = new ConnectionProvider(props)

  def addTrigger(trigger:Trigger, actualStart:Option[Date], misfire:Boolean) {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""INSERT INTO trigger_history(trigger_name, trigger_group, scheduled_start, actual_start, finish, misfire) VALUES(?, ?, ?, ?, ?, ?)""")
      prepared.setString(1, trigger.getKey.getName)
      prepared.setString(2, trigger.getKey.getGroup)
      prepared.setTimestamp(3, new Timestamp(trigger.getStartTime.getTime))
      prepared.setTimestamp(4, actualStart.map(date => new Timestamp(date.getTime)).getOrElse(null))
      prepared.setTimestamp(5, new Timestamp(System.currentTimeMillis))
      prepared.setBoolean(6, misfire)
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
      val prepared = connection.prepareStatement("""SELECT * FROM trigger_history WHERE trigger_name=? AND trigger_group=? ORDER BY schedule_start DESC LIMIT 100""")
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
          rs.getInt("misfire")
        )
      }
      result
    } catch {
      case e:Exception => logger.error("error in retrieving triggers")
      List()
    } finally {
      connection.close()
    }
  }
}

