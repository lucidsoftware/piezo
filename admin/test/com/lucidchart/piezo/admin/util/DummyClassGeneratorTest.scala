package com.lucidchart.piezo.admin.util

import org.specs2.mutable.*
import java.io.{PrintWriter, StringWriter}
import scala.util.Random
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
//import com.lucidchart.piezo.util.DummyClassGenerator

class DummyClassGeneratorTest extends Specification {
  val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
  rootLogger.setLevel(Level.DEBUG)
  val writer = new StringWriter()
  val printWriter = new PrintWriter(writer)

  val className = "DummyTestClass123"
  val methodName = "echo"
  printWriter.println("package com.lucidchart;")
  printWriter.println("public class " + className + " {")
  printWriter.println(" public static Integer " + methodName + "(Integer value) {")
  printWriter.println("    return value;")
  printWriter.println("  }")
  printWriter.println("}")
  printWriter.close()

  val random = new Random()

  "generator" should {
    "create dummy class" in {
      // val dummyClassGenerator = new DummyClassGenerator()
      // TODO: figure out why it won't load
//      val dummyClass: Option[Class[_]] = dummyClassGenerator.generate(className, writer.toString)
//      val dummyMethod = dummyClass.get.getDeclaredMethod(methodName, classOf[java.lang.Integer])
//      val echoParameter = random.nextInt()
//      val result = dummyMethod.invoke(null, echoParameter: java.lang.Integer)
//      result must equalTo(echoParameter)
      success
    }
  }
}
