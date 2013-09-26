package com.lucidchart.piezo.jobs.monitoring

import org.quartz.{JobExecutionContext, Job}
import java.io.{File, FileWriter}
import java.util.Date
import org.slf4j.LoggerFactory

class HeartBeat extends Job {
  def execute(context:JobExecutionContext) {
    LoggerFactory.getLogger(this.getClass).info((System.currentTimeMillis / 1000).toString)
  }
}
