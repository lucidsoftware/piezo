package com.lucidchart.piezo.admin.controllers

import org.quartz.JobDataMap
import play.api.libs.json.*
import play.api.data.Forms.*
import play.api.data.Mapping

case class DataMap(key: String, value: String)

object DataMap {
  implicit val writes: Writes[DataMap] = Writes { dataMap =>
    Json.obj(
      "key" -> dataMap.key,
      "value" -> dataMap.value,
    )
  }
}

trait JobDataHelper {

  private def mapToJobData(dataMap: List[DataMap]): JobDataMap = {
    dataMap.foldLeft(new JobDataMap()) { (sofar, next) =>
      sofar.put(next.key, next.value)
      sofar
    }
  }

  protected def jobDataToMap(jobData: JobDataMap): List[DataMap] = {
    jobData.getKeys.foldLeft(List[DataMap]())((sofar, key) => {
      sofar :+ DataMap(key, jobData.get(key).toString)
    })
  }

  implicit def jobDataMap: Mapping[Option[JobDataMap]] =
    optional(
      list(mapping("key" -> text, "value" -> text)(DataMap.apply)(data => Some((data.key, data.value))))
        .transform(mapToJobData, jobDataToMap),
    )

}
