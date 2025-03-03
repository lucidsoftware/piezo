package com.lucidchart.piezo.admin.models

import com.lucidchart.piezo.*
import org.quartz.utils.DBConnectionManager

/**
 * Components for all of the DB models
 */
class ModelComponents(getConnection: () => java.sql.Connection) {
  lazy val triggerHistoryModel: TriggerHistoryModel = new TriggerHistoryModel(
    getConnection,
  )
  lazy val jobHistoryModel: JobHistoryModel = new JobHistoryModel(getConnection)
  lazy val triggerMonitoringModel: TriggerMonitoringModel =
    new TriggerMonitoringModel(getConnection)
}

object ModelComponents {

  def forDataSource(dataSource: String): ModelComponents = {
    val manager = DBConnectionManager.getInstance()
    new ModelComponents(() => manager.getConnection(dataSource))
  }
}
