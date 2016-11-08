package com.lucidchart.piezo

import com.lucidchart.piezo.TriggerMonitoringPriority.TriggerMonitoringPriority
import java.util.Properties
import org.quartz.{Trigger, TriggerKey}
import org.slf4j.LoggerFactory

object TriggerMonitoringPriority extends Enumeration {
  type TriggerMonitoringPriority = Value
  val Off = Value(0, "Off")
  val Low = Value(1, "Low")
  val Medium = Value(2, "Medium")
  val High = Value(3, "High")
}

class TriggerMonitoringPriorityModel(props: Properties) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val connectionProvider = new ConnectionProvider(props)

  def setTriggerMonitoringPriority(trigger: Trigger, triggerMonitoringPriority: TriggerMonitoringPriority): Int = {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""
        INSERT INTO trigger_monitoring_priority
          (trigger_name, trigger_group, priority)
        VALUES
          (?, ?, ?)
        ON DUPLICATE KEY UPDATE
          priority = values(priority);
      """)
      prepared.setString(1, trigger.getKey.getName)
      prepared.setString(2, trigger.getKey.getGroup)
      prepared.setInt(3, triggerMonitoringPriority.id)
      prepared.executeUpdate()
    } catch {
      case e: Exception => logger.error(
        s"Error setting trigger monitoring priority. " +
        s"Trigger name: ${trigger.getKey.getName} group: ${trigger.getKey.getGroup}",
        e
      )
      0
    } finally {
      connection.close()
    }
  }

  def deleteTriggerMonitoringPriority(trigger: Trigger): Int = {
    deleteTriggerMonitoringPriority(trigger.getKey)
  }

  def deleteTriggerMonitoringPriority(triggerKey: TriggerKey): Int = {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""
        DELETE
        FROM trigger_monitoring_priority
        WHERE
          trigger_name = ?
          AND trigger_group = ?
      """)
      prepared.setString(1, triggerKey.getName)
      prepared.setString(2, triggerKey.getGroup)
      prepared.executeUpdate()
    } catch {
      case e: Exception => {
        logger.error(
          s"Error deleting trigger monitoring priority. " +
            s"Trigger name: ${triggerKey.getName} group: ${triggerKey.getGroup}",
          e
        )
        0
      }
    } finally {
      connection.close()
    }
  }

  def getTriggerMonitoringPriority(trigger: Trigger): Option[TriggerMonitoringPriority] = {
    val connection = connectionProvider.getConnection

    try {
      val prepared = connection.prepareStatement("""
        SELECT *
        FROM trigger_monitoring_priority
        WHERE
          trigger_name = ?
          AND trigger_group = ?
      """)
      prepared.setString(1, trigger.getKey.getName)
      prepared.setString(2, trigger.getKey.getGroup)
      val rs = prepared.executeQuery()
      if (rs.first()) {
        TriggerMonitoringPriority.values.find(_.id == rs.getInt("priority"))
      } else {
        None
      }
    } catch {
      case e: Exception => {
        logger.error(
          s"Error retrieving trigger monitoring priority. " +
          s"Trigger name: ${trigger.getKey.getName} group: ${trigger.getKey.getGroup}",
          e
        )
        None
      }
    } finally {
      connection.close()
    }
  }
}

