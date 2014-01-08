package com.lucidchart.piezo.admin.controllers

import org.quartz._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import com.lucidchart.piezo.GeneratorClassLoader

class JobFormHelper() {
  def jobFormApply(name: String, group: String, jobClass: String, description: String, durable: Boolean, requestRecovery: Boolean, dataMap: Option[List[DataMap]]): JobDetail = {

    val classLoader = new GeneratorClassLoader()
    classLoader.initialize
    val jobClassObject = classLoader.loadClass(jobClass)

    val jobDataMap = dataMap.map { _.foldLeft(new JobDataMap()) { (sofar, next) =>
      sofar.put(next.key, next.value)
      sofar
    }}

    val newJob: JobDetail = JobBuilder.newJob(jobClassObject.asSubclass(classOf[Job]))
      .withIdentity(name, group)
      .withDescription(description)
      .requestRecovery(requestRecovery)
      .storeDurably(durable)
      .usingJobData(jobDataMap.getOrElse(new JobDataMap()))
      .build()
    newJob
  }

  def jobFormUnapply(job: JobDetail): Option[(String, String, String, String, Boolean, Boolean, Option[List[DataMap]])] = {
    val description = if (job.getDescription() == null) "" else job.getDescription()

    val dataMap = job.getJobDataMap.getKeys.foldLeft(List[DataMap]())((sofar, key) => {
      sofar :+ DataMap(key, job.getJobDataMap.getString(key))
    })

    Some((job.getKey.getName(), job.getKey.getGroup(), job.getJobClass.toString.replace("class ", ""), description, job.isDurable(), job.requestsRecovery(), Some(dataMap)))
  }

  case class DataMap(key: String, value: String)

  def buildJobForm() = Form[JobDetail](
    mapping(
      "name" -> nonEmptyText(),
      "group" -> nonEmptyText(),
      "class" -> nonEmptyText(),
      "description" -> text(),
      "durable" -> boolean,
      "requests-recovery" -> boolean,
      "job-data-map" -> optional(list(mapping("key" -> text, "value" -> text)(DataMap.apply)(DataMap.unapply)))
    )(jobFormApply)(jobFormUnapply)
  )
}
