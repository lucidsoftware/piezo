package com.lucidchart.piezo.jobs.exec

import java.util.Scanner
import org.quartz.{Job, JobExecutionContext}
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters.*

/**
  * When creating job's data map, choose keys in alphabetical order
  * for corresponding values to be executed in the correct order
  */
class RunExec extends Job {
  val logger = LoggerFactory.getLogger(this.getClass)

  def execute(context: JobExecutionContext): Unit = {
    val jobDataMap = context.getJobDetail.getJobDataMap
    val sortedDataList = jobDataMap.entrySet.asScala.toList.sortBy(_.getKey)
    val commands: java.util.List[String] = sortedDataList.map(entry => entry.getValue.toString).asJava
    val cmdProcess = new ProcessBuilder(commands).start
    cmdProcess.waitFor
    val result = new Scanner(cmdProcess.getInputStream,"UTF-8").useDelimiter("\\A").next()
    logger.info("Executable output: " + result)
  }
}
