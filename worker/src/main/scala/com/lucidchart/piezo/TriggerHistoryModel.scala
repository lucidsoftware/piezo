package com.lucidchart.piezo

import java.time.Instant
import java.sql.Connection
import org.quartz.TriggerKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory

case class TriggerRecord(
  name: String,
  group: String,
  scheduled_start: Instant,
  actual_start: Option[Instant],
  finish: Instant,
  misfire: Int,
  fire_instance_id: String,
)

class TriggerHistoryModel(getConnection: () => Connection) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def addTrigger(
    triggerKey: TriggerKey,
    triggerFireTime: Option[Instant],
    actualStart: Option[Instant],
    misfire: Boolean,
    fireInstanceId: Option[String],
  ): Unit = {
    val connection = getConnection()
    try {
      val prepared = connection.prepareStatement(
        """
          INSERT INTO trigger_history(
            trigger_name,
            trigger_group,
            scheduled_start,
            actual_start,
            finish,
            misfire,
            fire_instance_id
          ) VALUES(?, ?, ?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            trigger_name = Values(trigger_name),
            trigger_group = Values(trigger_group),
            scheduled_start = Values(scheduled_start),
            actual_start = Values(actual_start),
            finish = Values(finish),
            misfire = Values(misfire),
            fire_instance_id = Values(fire_instance_id)
        """.stripMargin,
      )
      prepared.setString(1, triggerKey.getName)
      prepared.setString(2, triggerKey.getGroup)
      prepared.setObject(3, triggerFireTime.getOrElse(Instant.now()))
      prepared.setObject(4, actualStart.getOrElse(null))
      prepared.setObject(5, Instant.now())
      prepared.setBoolean(6, misfire)
      prepared.setString(7, fireInstanceId.getOrElse(""))
      prepared.executeUpdate()
    } catch {
      case e: Exception => logger.error("error in recording end of trigger", e)
    } finally {
      connection.close()
    }
  }

  def deleteTriggers(minScheduledStart: Instant): Int = {
    val connection = getConnection()
    try {
      val prepared = connection.prepareStatement("""DELETE FROM trigger_history WHERE scheduled_start < ?""")
      prepared.setObject(1, minScheduledStart)
      prepared.executeUpdate()
    } catch {
      case e: Exception =>
        logger.error("error deleting trigger histories", e)
        0
    } finally {
      connection.close()
    }
  }

  def getTrigger(triggerKey: TriggerKey): List[TriggerRecord] = {
    val connection = getConnection()

    try {
      val prepared = connection.prepareStatement(
        """SELECT * FROM trigger_history WHERE trigger_name=? AND trigger_group=? ORDER BY scheduled_start DESC LIMIT 100""",
      )
      prepared.setString(1, triggerKey.getName)
      prepared.setString(2, triggerKey.getGroup)
      val rs = prepared.executeQuery()

      var result = List[TriggerRecord]()
      while (rs.next()) {
        result :+= new TriggerRecord(
          rs.getString("trigger_name"),
          rs.getString("trigger_group"),
          rs.getTimestamp("scheduled_start").toInstant,
          Option(rs.getTimestamp("actual_start")).map(_.toInstant),
          rs.getTimestamp("finish").toInstant,
          rs.getInt("misfire"),
          rs.getString("fire_instance_id"),
        )
      }
      result
    } catch {
      case e: Exception =>
        logger.error("error in retrieving triggers", e)
        List()
    } finally {
      connection.close()
    }
  }
}
