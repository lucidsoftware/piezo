package com.lucidchart.piezo.admin.controllers

import org.quartz.JobDataMap
import play.api.data.Form
import play.api.data.Forms._

case class DataMap(key: String, value: String)

trait JobDataHelper {

  private def mapToJobData(dataMap: List[DataMap]): JobDataMap = {
    dataMap.foldLeft(new JobDataMap()) { (sofar, next) =>
      sofar.put(next.key, next.value)
      sofar
    }
  }

  private def jobDataToMap(jobData: JobDataMap) = {
    jobData.getKeys.foldLeft(List[DataMap]())((sofar, key) => {
      sofar :+ DataMap(key, jobData.get(key).toString)
    })
  }

  implicit def jobDataMap = {
    optional(list(mapping("key" -> text, "value" -> text)(DataMap.apply)(DataMap.unapply))
      .transform(mapToJobData, jobDataToMap))
  }

}