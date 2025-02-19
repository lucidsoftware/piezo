package com.lucidchart.piezo.admin.utils
import play.api.libs.json.*
import org.quartz.*
import scala.collection.JavaConverters.*
import com.lucidchart.piezo.admin.controllers.{JobDataHelper, JobFormHelper, TriggerHelper}
import com.lucidchart.piezo.TriggerMonitoringModel

object JobDetailHelper extends JobDataHelper {
  lazy val jobFormHelper = new JobFormHelper()

  implicit def jobDetailWrites(
    triggers: Seq[Trigger],
    triggerMonitoringModel: TriggerMonitoringModel,
  ): Writes[JobDetail] = Writes[JobDetail] { jobDetail =>
    val jobDataMap = jobDetail.getJobDataMap

    val jobKey = jobDetail.getKey
    Json.obj(
      "group" -> jobKey.getGroup,
      "name" -> jobKey.getName,
      "description" -> jobDetail.getDescription,
      "class" -> jobDetail.getJobClass.getName,
      "concurrent" -> jobDetail.isConcurrentExecutionDisallowed,
      "durable" -> jobDetail.isDurable,
      "requests-recovery" -> jobDetail.requestsRecovery,
      "job-data-map" -> Json.toJson(jobDataToMap(jobDataMap)),
      "triggers" -> Json.toJson(triggers)(TriggerHelper.writesTriggerSeq(triggerMonitoringModel)),
    )
  }

  implicit def jobDetailSeqWrites(
    triggers: Seq[Trigger],
    triggerMonitoringModel: TriggerMonitoringModel,
  ): Writes[Seq[JobDetail]] =
    Writes.seq(jobDetailWrites(triggers, triggerMonitoringModel))
}
