package com.lucidchart.piezo.admin.controllers

import org.quartz.*
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.*
import com.lucidchart.piezo.GeneratorClassLoader

class JobFormHelper extends JobDataHelper {
  def jobFormApply(name: String, group: String, jobClass: String, description: String, durable: Boolean, requestRecovery: Boolean, jobData: Option[JobDataMap]): JobDetail = {

    val classLoader = new GeneratorClassLoader()
    classLoader.initialize
    val jobClassObject = classLoader.loadClass(jobClass)

    val newJob: JobDetail = JobBuilder.newJob(jobClassObject.asSubclass(classOf[Job]))
      .withIdentity(name, group)
      .withDescription(description)
      .requestRecovery(requestRecovery)
      .storeDurably(durable)
      .usingJobData(jobData.getOrElse(new JobDataMap()))
      .build()
    newJob
  }

  def jobFormUnapply(job: JobDetail): Option[(String, String, String, String, Boolean, Boolean, Option[JobDataMap])] = {
    val description = if (job.getDescription() == null) "" else job.getDescription()

    Some((job.getKey.getName(), job.getKey.getGroup(), job.getJobClass.toString.replace("class ", ""), description, job.isDurable(), job.requestsRecovery(), Some(job.getJobDataMap)))
  }

  def buildJobForm = Form[JobDetail](
    mapping(
      "name" -> nonEmptyText(),
      "group" -> nonEmptyText(),
      "class" -> nonEmptyText(),
      "description" -> text(),
      "durable" -> boolean,
      "requests-recovery" -> boolean,
      "job-data-map" -> jobDataMap
    )(jobFormApply)(jobFormUnapply)
  )
}
