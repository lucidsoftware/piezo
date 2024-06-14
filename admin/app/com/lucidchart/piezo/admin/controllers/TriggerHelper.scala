package com.lucidchart.piezo.admin.controllers

import com.lucidchart.piezo.{TriggerMonitoringModel, TriggerMonitoringPriority}
import com.lucidchart.piezo.admin.models.TriggerType
import scala.jdk.CollectionConverters._

import org.quartz.TriggerKey
import org.quartz.{CronTrigger, SimpleTrigger}
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.Scheduler
import org.quartz.Trigger
import play.api.libs.json._


import scala.collection.mutable

object TriggerHelper {
  def getTriggersByGroup(scheduler: Scheduler): mutable.Buffer[(String, List[TriggerKey])] = {
    val triggersByGroup =
      for (groupName <- scheduler.getTriggerGroupNames.asScala) yield {
        val triggers: List[TriggerKey] = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName)).asScala.toList
        val sortedTriggers: List[TriggerKey] = triggers.sortBy(triggerKey => triggerKey.getName)
        (groupName, sortedTriggers)
      }
    triggersByGroup.sortBy(groupList => groupList._1)
  }

  def writesTrigger(monitoringModel: TriggerMonitoringModel): Writes[Trigger] = Writes { trigger =>
    val triggerKey = trigger.getKey
    val triggerType = TriggerType(trigger)
    val schedule = triggerType match {
      case TriggerType.Cron => {
        val cronTrigger = trigger.asInstanceOf[CronTrigger]
        Json.obj("cron" ->
          Json.obj(
            "cronExpression" -> cronTrigger.getCronExpression
          )
        )

      }
      case TriggerType.Simple => {
        val simpleTrigger = trigger.asInstanceOf[SimpleTrigger]
        Json.obj(
          "simple" -> Json.obj(
            "repeatInterval" -> simpleTrigger.getRepeatInterval,
            "repeatCount" -> simpleTrigger.getRepeatCount
          )
        )
      }
      case _ => Json.obj()
    }

    val (monitoringPriority, maxSecondsInError, monitoringTeam) = monitoringModel.getTriggerMonitoringRecord(
      trigger.getKey,
    ).map { monitoringRecord =>
      (monitoringRecord.priority, monitoringRecord.maxSecondsInError, monitoringRecord.monitoringTeam)
    }.getOrElse((TriggerMonitoringPriority.Low, 300, None))
    val jobDataMap = trigger.getJobDataMap
    val job = trigger.getJobKey
    Json.obj(
      "triggerType" -> triggerType.toString,
      "jobGroup" -> job.getGroup,
      "jobName" -> job.getName,
      "group" -> triggerKey.getGroup,
      "name" -> triggerKey.getName,
      "description" -> trigger.getDescription,
      "job-data-map" -> JsObject(jobDataMap.getKeys.toSeq.map(key => key -> JsString(jobDataMap.getString(key)))),
      "triggerMonitoringPriority" -> monitoringPriority.name,
      "triggerMaxErrorTime" -> maxSecondsInError,
      "triggerMonitoringTeam" -> monitoringTeam,
    ) ++ schedule
  }

  def writesTriggerSeq(monitoringModel: TriggerMonitoringModel) = Writes.seq(writesTrigger(monitoringModel))
}
