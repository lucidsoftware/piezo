package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable.*

import play.api.test.*
import play.api.test.Helpers.*
import com.lucidchart.piezo.jobs.monitoring.HeartBeat
import com.lucidchart.piezo.WorkerSchedulerFactory
import TestUtil.*
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import org.quartz.Job
import com.lucidchart.piezo.util.DummyClassGenerator

/**
 * Add your spec here. You can mock out a whole application including requests, plugins etc. For more information,
 * consult the wiki.
 */
class JobsControllerTest extends Specification {
  val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
  rootLogger.setLevel(Level.DEBUG)

  "Jobs" should {
    "create dummy job class" in {
      // TODO: figure out how to set the classpath in a test
//      val rootPackageClassName = "foo"
//      val rootPackageClassSource = Jobs.getDummyJobSource(rootPackageClassName)
//      val dummyClassGenerator = new DummyClassGenerator()
//      val rootPackageDummyClass: Option[Class[_]] = dummyClassGenerator.generate(rootPackageClassName, rootPackageClassSource)
//      rootPackageDummyClass.get.getName() must equalTo(rootPackageClassName)
//      rootPackageDummyClass.get.getInterfaces.contains(classOf[Job]) must beTrue

//      val nonRootPackageClassName = "bar.foo"
//      val nonRootPackageClassSource = Jobs.getDummyJobSource(nonRootPackageClassName)
//      val dummyClassGenerator2 = new DummyClassGenerator()
//      val nonRootPackageDummyClass: Option[Class[_]] = dummyClassGenerator2.generate(nonRootPackageClassName, nonRootPackageClassSource)
//      nonRootPackageDummyClass.get.getName() must equalTo(nonRootPackageClassName)
      success
    }
  }
}
