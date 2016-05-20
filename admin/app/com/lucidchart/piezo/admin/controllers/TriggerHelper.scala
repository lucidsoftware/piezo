package com.lucidchart.piezo.admin.controllers

import collection.JavaConverters._

import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.Scheduler

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
}
