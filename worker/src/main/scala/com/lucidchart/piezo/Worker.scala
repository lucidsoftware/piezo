package com.lucidchart.piezo

import com.timgroup.statsd.NonBlockingStatsDClientBuilder
import java.io.*
import java.util.Properties
import java.util.concurrent.{Semaphore, TimeUnit}
import org.quartz.Scheduler
import org.slf4j.LoggerFactory
import scala.util.Try
import scala.util.control.NonFatal
import org.quartz.utils.DBConnectionManager
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.SECONDS
import java.sql.Connection
import org.quartz.SchedulerContext

/**
 * To stop the worker without stopping SBT: Ctrl+D Enter
 */
object Worker {

  type GetConnection = () => Connection

  /**
   * A key to lookup the connectionManager in the SchedulerContext
   */
  private val PiezoConnectionKey = "com.lucidchart.piezo.getConnection"

  private val logger = LoggerFactory.getLogger(this.getClass)
  private[piezo] val runSemaphore = new Semaphore(0)
  private val shutdownSemaphore = new Semaphore(1)
  private[piezo] val dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(UTC)

  def main(args: Array[String]): Unit = {
    logger.info("worker starting")

    shutdownSemaphore.acquire()

    writePID()
    setupShutdownHandler()

    val schedulerFactory: WorkerSchedulerFactory = new WorkerSchedulerFactory()
    val scheduler = schedulerFactory.getScheduler()
    val props = schedulerFactory.props
    val useDatadog =
      Try(props.getProperty("com.lucidchart.piezo.statsd.useDatadog", "false").toBoolean).getOrElse(false)
    val statsd = new NonBlockingStatsDClientBuilder()
      .prefix(props.getProperty("com.lucidchart.piezo.statsd.prefix", "applications.piezo.worker"))
      .hostname(props.getProperty("com.lucidchart.piezo.statsd.host", "localhost"))
      .port(Try(props.getProperty("com.lucidchart.piezo.statsd.port").toInt).getOrElse(8125))
      .build()

    val connectionManager = DBConnectionManager.getInstance()

    val piezoDataSource = {
      var source = props.getProperty("com.lucidchart.piezo.dataSource")
      if (source == null) {
        source = props.getProperty("org.quartz.jobStore.dataSource")
      }
      source
    }

    val getConnection = () => connectionManager.getConnection(piezoDataSource)

    scheduler.getContext().put(PiezoConnectionKey, getConnection)

    scheduler.getListenerManager.addJobListener(new WorkerJobListener(getConnection, statsd, useDatadog))
    scheduler.getListenerManager.addTriggerListener(new WorkerTriggerListener(getConnection, statsd, useDatadog))
    run(scheduler, props)

    logger.info("exiting")

    shutdownSemaphore.release()

    System.exit(0)
  }

  def connectionFactory(context: SchedulerContext): () => Connection = {
    context.get(PiezoConnectionKey).asInstanceOf[() => Connection]
  }

  private[piezo] def run(
    scheduler: Scheduler,
    properties: Properties,
    heartbeatSeconds: Int = 60,
    semaphorePermitsToStop: Int = 1,
  ): Unit = {
    val heartbeatFile = properties.getProperty("com.lucidchart.piezo.heartbeatFile")
    if (heartbeatFile == null) {
      logger.trace("No heartbeat file specified")
    }

    try {
      scheduler.start()
      logger.info("scheduler started")
      val reader = new InputStreamReader(System.in)

      var acquired = false
      while (!acquired) {
        try {
          acquired = runSemaphore.tryAcquire(semaphorePermitsToStop, 1, TimeUnit.SECONDS)
          if (!acquired) {
            if (System.currentTimeMillis() / 1000 % heartbeatSeconds == 0) {
              if (heartbeatFile != null) {
                writeHeartbeat(heartbeatFile)
              }
              val currentJobs: Int = scheduler.getCurrentlyExecutingJobs.size
              logger.info("worker heartbeat - currently running " + currentJobs + " jobs")
            }
            if (reader.ready && System.in.read == -1) {
              logger.info("Received EOF on stdin")
              runSemaphore.release()
            }
          }
        } catch {
          case e: InterruptedException => logger.error("caught interruption exception: " + e)
          case e: Exception => logger.error("caught exception: " + e)
        }
      }
      scheduler.shutdown(true)
      logger.info("scheduler shutdown")
    } catch {
      case e: Exception => logger.error("exception caught scheduling jobs: " + e)
    }
  }

  private[piezo] def writeHeartbeat(filePath: String): Unit = {
    try {
      val file = new File(filePath)
      file.getParentFile.mkdirs()
      val fileWrite = new FileWriter(file)
      val heartbeatTime = dtf.format(Instant.now().truncatedTo(SECONDS))
      fileWrite.write(heartbeatTime)
      fileWrite.close()
    } catch {
      case NonFatal(e) => logger.warn(s"Exception caught writing heartbeat timestamp to file $filePath)", e)
    }
  }

  private def writePID() = {
    val location = getClass.getProtectionDomain.getCodeSource.getLocation
    val applicationPath = location.getFile()
    java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@').headOption.map { pid =>
      val pidFile =
        Option(System.getProperty("pidfile.path")).map(new File(_)).getOrElse(new File(applicationPath, "RUNNING_PID"))

      logger.info("process ID is " + pid)
      logger.info("pid file: " + pidFile.getAbsolutePath)

      if (pidFile.getAbsolutePath != "/dev/null") {
        new FileOutputStream(pidFile).write(pid.getBytes)
        Runtime.getRuntime.addShutdownHook(new Thread {
          override def run: Unit = {
            pidFile.delete()
          }
        })
      }
    }
  }

  private def setupShutdownHandler(): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        logger.info("received shutdown signal")
        runSemaphore.release()
        shutdownSemaphore.acquire()
      }
    })
  }
}
