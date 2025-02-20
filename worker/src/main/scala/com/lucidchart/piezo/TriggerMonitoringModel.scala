package com.lucidchart.piezo

import com.lucidchart.piezo.TriggerMonitoringPriority.TriggerMonitoringPriority
import java.util.{Date, Properties}
import org.quartz.TriggerKey
import org.slf4j.LoggerFactory
import org.slf4j.Logger

object TriggerMonitoringPriority {
  case class Value(id: Int, name: String) {
    override def toString: String = name
  }
  type TriggerMonitoringPriority = Value
  val Off: Value = Value(0, "Off")
  val Low: Value = Value(1, "Low")
  val High: Value = Value(3, "High")

  val values: List[Value] = List(Off, Low, High)

  // map values that formerly identified a Medium priority to Low
  val valuesById: Map[Int, Value] = Map(2 -> Low) ++ values.map(p => p.id -> p)
  val valuesByName: Map[String, Value] = Map("Medium" -> Low) ++ values.map(p => p.name -> p)

  def withName: Function[String, Value] = valuesByName
}

case class TriggerMonitoringRecord(
  triggerName: String,
  triggerGroup: String,
  priority: TriggerMonitoringPriority,
  maxSecondsInError: Int,
  monitoringTeam: Option[String],
  created: Date,
  modified: Date,
)

class TriggerMonitoringModel(props: Properties) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val connectionProvider = new ConnectionProvider(props)

  def setTriggerMonitoringRecord(
    triggerKey: TriggerKey,
    triggerMonitoringPriority: TriggerMonitoringPriority,
    maxSecondsInError: Int,
    monitoringTeam: Option[String],
  ): Int = {
    val connection = connectionProvider.getConnection
    try {
      val prepared = connection.prepareStatement("""
        INSERT INTO trigger_monitoring_priority
          (trigger_name, trigger_group, priority, max_error_time, monitoring_team)
        VALUES
          (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
          priority = values(priority),
          max_error_time = values(max_error_time),
          monitoring_team = values(monitoring_team)
      """)
      prepared.setString(1, triggerKey.getName)
      prepared.setString(2, triggerKey.getGroup)
      prepared.setInt(3, triggerMonitoringPriority.id)
      prepared.setInt(4, maxSecondsInError)
      monitoringTeam match {
        case Some(team) => prepared.setString(5, team)
        case None => prepared.setNull(5, java.sql.Types.VARCHAR)
      }
      prepared.executeUpdate()
    } catch {
      case e: Exception =>
        logger.error(
          s"Error setting trigger monitoring priority. " +
            s"Trigger name: ${triggerKey.getName} group: ${triggerKey.getGroup}",
          e,
        )
        0
    } finally {
      connection.close()
    }
  }

  def deleteTriggerMonitoringRecord(triggerKey: TriggerKey): Int = {
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
          e,
        )
        0
      }
    } finally {
      connection.close()
    }
  }

  def getTriggerMonitoringRecord(triggerKey: TriggerKey): Option[TriggerMonitoringRecord] = {
    val connection = connectionProvider.getConnection

    try {
      val prepared = connection.prepareStatement("""
        SELECT *
        FROM trigger_monitoring_priority
        WHERE
          trigger_name = ?
          AND trigger_group = ?
      """)
      prepared.setString(1, triggerKey.getName)
      prepared.setString(2, triggerKey.getGroup)
      val rs = prepared.executeQuery()
      if (rs.next()) {
        TriggerMonitoringPriority.valuesById.get(rs.getInt("priority")).map { priority =>
          TriggerMonitoringRecord(
            rs.getString("trigger_name"),
            rs.getString("trigger_group"),
            priority,
            rs.getInt("max_error_time"),
            Option(rs.getString("monitoring_team")),
            rs.getDate("created"),
            rs.getDate("modified"),
          )
        }
      } else {
        None
      }
    } catch {
      case e: Exception => {
        logger.error(
          s"Error retrieving trigger monitoring priority. " +
            s"Trigger name: ${triggerKey.getName} group: ${triggerKey.getGroup}",
          e,
        )
        None
      }
    } finally {
      connection.close()
    }
  }
}
