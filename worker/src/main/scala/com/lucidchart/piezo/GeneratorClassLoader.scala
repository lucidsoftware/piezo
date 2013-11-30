package com.lucidchart.piezo

import org.quartz.simpl.CascadingClassLoadHelper
import java.io.{PrintWriter, StringWriter}
import org.slf4j.LoggerFactory
import com.lucidchart.piezo.util.DummyClassGenerator

class GeneratorClassLoader extends CascadingClassLoadHelper{
  val logger = LoggerFactory.getLogger(this.getClass)
  val dummyClassGenerator = new DummyClassGenerator()

  private[piezo] def getDummyJobSource(name: String): String = {
    val writer = new StringWriter()
    val printWriter = new PrintWriter(writer)

    val classNameParts = name.split('.')
    if (classNameParts.length > 1) {
      printWriter.println("package " + classNameParts.clone.dropRight(1).mkString(".") + ";")
    }
    printWriter.println("import org.quartz.Job;")
    printWriter.println("import org.quartz.JobExecutionContext;")
    printWriter.println("import org.quartz.JobExecutionException;")
    printWriter.println("public class " + classNameParts.last + " implements Job {")
    printWriter.println("  public void execute(JobExecutionContext context) throws JobExecutionException {")
    printWriter.println("    throw new UnsupportedOperationException();")
    printWriter.println("  }")
    printWriter.println("}")
    printWriter.close()
    writer.toString()
  }

  override def loadClass(name: String): Class[_] = {
    try {
      super.loadClass(name)
    }
    catch {
      case e: ClassNotFoundException => {
        logger.info("Could not find job class " + e.getMessage + ". Trying to generate dummy class now.")
        val source = getDummyJobSource(e.getMessage) //TODO: check how often this is called for a single class
        val job = dummyClassGenerator.generate(e.getMessage, source)
        job.map(jobValue => logger.info("Generated class " + jobValue.getName))
        if (job.isDefined) job.get else null
      }
    }
  }
}
