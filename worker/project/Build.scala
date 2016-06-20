import java.io.File

import sbt._
import sbt.Keys._

object Build extends Build {
  val pidfile = File.createTempFile("piezoWorkerPid", null)
  val heartbeat = File.createTempFile("piezoHeartbeat", null)
  lazy val root =
    Project("root", file("."))
    .settings(
      fork := true,
      javaOptions in (Compile, run) ++= Seq(
        s"-Dpidfile.path=${pidfile.getAbsolutePath}",
        s"-Dorg.quartz.properties=${(baseDirectory.value / "src/main/resources/quartz.properties").getAbsolutePath}",
        s"-Dcom.lucidchart.piezo.heartbeatfile=${heartbeat.getAbsolutePath}"
      )
    )

  val excludeFileRegex = """(.*)\.(properties|sql|sh)$""".r
}