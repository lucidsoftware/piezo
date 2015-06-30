package com.lucidchart.piezo

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import org.quartz.Scheduler
import java.util.concurrent.{TimeUnit, Semaphore}
import java.io.{FileWriter, FileOutputStream, File}

import scala.util.control.NonFatal


object Worker {
  protected val logger = LoggerFactory.getLogger(this.getClass)
  protected[piezo] val runSemaphore = new Semaphore(0)
  protected[piezo] val dtf = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()

  def main(args: Array[String]) {
    logger.info("worker starting")

    writePID()
    setupShutdownHandler()

    val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
    val scheduler = schedulerFactory.getScheduler()
    val props = schedulerFactory.props
    scheduler.getListenerManager.addJobListener(new WorkerJobListener(props))
    scheduler.getListenerManager.addTriggerListener(new WorkerTriggerListener(props))
    run(scheduler)

    logger.info("exiting")
  }

  protected[piezo] def run(scheduler: Scheduler, heartbeatSeconds: Int = 60, semaphorePermitsToStop: Int = 1) {
    val heartbeatFile = System.getProperty("com.lucidchart.piezo.heartbeatfile")
    if (heartbeatFile == null) {
      logger.trace("No heartbeat file specified")
    }

    try {
      scheduler.start()
      logger.info("scheduler started")
      var acquired = false
      while (!acquired) {
        try {
          acquired = runSemaphore.tryAcquire(semaphorePermitsToStop, heartbeatSeconds, TimeUnit.SECONDS)
          if (!acquired) {
            if (heartbeatFile != null) {
              writeHeartbeat(heartbeatFile)
            }
            logger.info("worker heartbeat")
          }
        }
        catch {
          case e: InterruptedException => logger.error("caught interruption exception: " + e)
          case e: Exception => logger.error("caught exception: " + e)
        }
      }
      scheduler.shutdown(true)
      logger.info("scheduler shutdown")
    }
    catch {
      case e: Exception => logger.error("exception caught scheduling jobs: " + e)
    }
  }

  protected[piezo] def writeHeartbeat(filePath: String): Unit = {
    try {
      val file = new File(filePath)
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.print(new DateTime(System.currentTimeMillis()))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
    } catch {
      case NonFatal(e) => logger.warn("Exception caught writing heartbeat timestamp")
    }
  }

  protected def writePID() = {
    val location = getClass.getProtectionDomain.getCodeSource.getLocation
    val applicationPath = location.getFile()
    java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@').headOption.map { pid =>
      val pidFile = Option(System.getProperty("pidfile.path")).map(new File(_)).getOrElse(new File(applicationPath, "RUNNING_PID"))

      logger.info("process ID is " + pid)
      logger.info("pid file: " + pidFile.getAbsolutePath)

      if (pidFile.getAbsolutePath != "/dev/null") {
        new FileOutputStream(pidFile).write(pid.getBytes)
        Runtime.getRuntime.addShutdownHook(new Thread {
          override def run {
            pidFile.delete()
          }
        })
      }
    }
  }

  protected def setupShutdownHandler() {
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        logger.info("received shutdown signal")
        runSemaphore.release()
      }
    })
  }
}
