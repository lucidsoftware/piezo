package com.lucidchart.piezo

import org.slf4j.LoggerFactory
import org.quartz.Scheduler
import java.util.concurrent.{TimeUnit, Semaphore}
import java.io.FileOutputStream
import java.io.File

/**
	*/
object Worker {
  private val logger = LoggerFactory.getLogger(Worker.getClass)
  private[piezo] val runSemaphore = new Semaphore(0)

	def main(args: Array[String]) {
    logger.info("worker starting")

    writePID()
    setupShutdownHandler()

    val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory
    val scheduler = schedulerFactory.getScheduler
    val props = schedulerFactory.props
    scheduler.getListenerManager.addJobListener(new WorkerJobListener(props))
    scheduler.getListenerManager.addTriggerListener(new WorkerTriggerListener(props))
    run(scheduler)

    logger.info("exiting")
	}

  private[piezo] def run(scheduler: Scheduler) {
    try {
      scheduler.start()
      logger.info("scheduler started")
      var acquired = false
      while (!acquired) {
        try {
          acquired = runSemaphore.tryAcquire(60, TimeUnit.SECONDS)
          if (!acquired)
            logger.info("worker heartbeat")
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

  private def writePID() = {
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

  private def setupShutdownHandler() {
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        logger.info("received shutdown signal")
        runSemaphore.release()
      }
    })
  }
}
