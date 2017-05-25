import java.io.File

name := "piezo-worker"

mainClass := Some("com.lucidchart.piezo.Worker")

connectInput in run := true

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.7" % Provided,
  "net.databinder" %% "dispatch-http" % "0.8.10",
  "org.quartz-scheduler" % "quartz" % "2.1.7",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.specs2" %% "specs2" % "2.3.13" % Test,
  "mysql" % "mysql-connector-java" % "5.1.25",
  "javax.transaction" % "jta" % "1.1",
  "joda-time" % "joda-time" % "2.8.1",
  "org.joda" % "joda-convert" % "1.7",
  "com.typesafe" % "config" % "1.0.0",
  "com.datadoghq" % "java-dogstatsd-client" % "2.3"
)

fork := true

javaOptions ++= Seq(
  s"-Dpidfile.path=${File.createTempFile("piezoWorkerPid", null)}",
  s"-Dcom.lucidchart.piezo.heartbeatfile=${File.createTempFile("piezoHeartbeat", null)}",
  "-Dorg.quartz.properties=quartz.properties"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xlint"
)

unmanagedClasspath in Compile += sourceDirectory.value / "run" / "resources"

version := sys.props.getOrElse("build.version", "0.0-SNAPSHOT")
