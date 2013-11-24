package com.lucidchart.piezo.admin.util

import javax.tools.{Diagnostic, JavaFileObject, DiagnosticCollector, ToolProvider}
import scala.collection.JavaConversions._
import play.api.Logger
import java.net.{URL, URLClassLoader}
import java.io.File


class DummyClassGenerator {
  val logger = Logger(this.getClass)

  val outputPath = "/tmp" //TODO: change this to a platform independent tmp folder
  val compiler = ToolProvider.getSystemJavaCompiler()
  val diagnostics: DiagnosticCollector[JavaFileObject] = new DiagnosticCollector[JavaFileObject]()

  //TODO: Don't create a long hierarchy of class loaders
  private def updateClassLoader() {
    val currentThreadClassLoader: ClassLoader = Thread.currentThread().getContextClassLoader()

    // Add the output dir to the classpath
    // Chain the current thread classloader
    val urlClassLoader: URLClassLoader = new URLClassLoader(
      Array(new File(outputPath).toURI().toURL()), currentThreadClassLoader)

    // Replace the thread classloader - assumes you have permissions to do so
    Thread.currentThread().setContextClassLoader(urlClassLoader)
  }
  
  private def getClasspath() = {
    val classLoader = Thread.currentThread().getContextClassLoader()
    val urls = classLoader.asInstanceOf[URLClassLoader].getURLs()
    val buffer = new StringBuilder(1000)
    buffer.append(".")
    val separator = System.getProperty("path.separator")
    for (url <- urls) {
      buffer.append(separator).append(url.getFile)
    }
    buffer.toString()
  }

  def generate(name: String, source: String): Option[Class[_]] = {
    val file: JavaFileObject = new SourceFromString(name, source)
    val compilationUnits = List[JavaFileObject](file)
    val classpath = getClasspath()
    val options = List("-d", outputPath, "-classpath", classpath)
    val task = compiler.getTask(null, null, diagnostics, options, null, compilationUnits)
    val success = task.call()
    for (diagnostic <- diagnostics.getDiagnostics) {
      logger.debug("Result of compiling " + name)
      logger.debug(diagnostic.getCode)
      logger.debug(diagnostic.getKind.toString)
      logger.debug(diagnostic.getPosition.toString)
      logger.debug(diagnostic.getStartPosition.toString)
      logger.debug(diagnostic.getEndPosition.toString)
      logger.debug(diagnostic.getSource.toString)
      logger.debug(diagnostic.getMessage(null))
    }
    logger.debug("Success: " + success)

    updateClassLoader()

    if (success) {
      Some(Thread.currentThread().getContextClassLoader().loadClass(name))
    } else {
      None
    }
  }
}
