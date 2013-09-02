package com.lucidchart.piezo.jobs.monitoring

import org.quartz.{JobExecutionContext, Job}
import java.io.{File, FileWriter}
import java.util.Date
import org.slf4j.LoggerFactory

object HeartBeat {
  val statsd = new com.lucidchart.util.statsd.StatsD("applications.jobs.worker")
}

class HeartBeat extends Job {
  val statsKey = "heartbeat.executed"

  def execute(context:JobExecutionContext) {
    LoggerFactory.getLogger(this.getClass).info((System.currentTimeMillis / 1000).toString)
    HeartBeat.statsd.increment(statsKey)
  }
}
