package com.lucidchart.piezo.util

import javax.tools.{ JavaFileObject, DiagnosticCollector, ToolProvider}
import scala.collection.JavaConversions._
import java.net.{URLClassLoader}
import org.slf4j.LoggerFactory
import java.io.File


class DummyClassGenerator {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val tempDir: String = System.getProperty("java.io.tmpdir")
  val tempOutputDirName: String = tempDir + File.separator + "piezo"
  val tempOutputDir = new File(tempOutputDirName)
  if (!tempOutputDir.exists())
  {
    tempOutputDir.mkdir()
  }
  val urlClassLoader: URLClassLoader = new URLClassLoader(
    Array(tempOutputDir.toURI().toURL()), Thread.currentThread().getContextClassLoader())
  val compiler = ToolProvider.getSystemJavaCompiler()
  val diagnostics: DiagnosticCollector[JavaFileObject] = new DiagnosticCollector[JavaFileObject]()
  
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
    try {
      Some(urlClassLoader.loadClass(name))
    } catch {
      case e: ClassNotFoundException => {
        logger.debug("Generating class " + name)
        val file: JavaFileObject = new SourceFromString(name, source)
        val compilationUnits = List[JavaFileObject](file)
        val classpath = getClasspath()
        val options = List("-d", tempOutputDirName, "-classpath", classpath)
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

        if (success) {
          logger.info("Generated class " + name)
          Some(urlClassLoader.loadClass(name))
        } else {
          None
        }
      }
    }
  }
}
