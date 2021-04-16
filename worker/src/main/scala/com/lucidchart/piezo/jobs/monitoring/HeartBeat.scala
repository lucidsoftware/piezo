package com.lucidchart.piezo.jobs.monitoring

import org.quartz.{JobExecutionContext, Job}
import org.slf4j.LoggerFactory

class HeartBeat extends Job {
  def execute(context: JobExecutionContext): Unit = {
    LoggerFactory.getLogger(this.getClass).info((System.currentTimeMillis / 1000).toString)
  }
}
