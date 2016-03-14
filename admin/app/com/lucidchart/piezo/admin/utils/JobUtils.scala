package com.lucidchart.piezo.admin.utils

import org.quartz.JobDetail

object JobUtils {

  def cleanup(job: JobDetail):JobDetail = {
    if (job.getJobDataMap.containsKey("")) {
      job.getJobDataMap.remove("")
    }
    job
  }

}
