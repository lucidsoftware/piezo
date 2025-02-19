import java.io.File

name := "piezo-worker"

Compile / mainClass := Some("com.lucidchart.piezo.Worker")

run / connectInput := true

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.5.16" % Provided,
  "org.quartz-scheduler" % "quartz" % "2.5.0",
  "org.quartz-scheduler" % "quartz-jobs" % "2.5.0",
  "com.zaxxer" % "HikariCP" % "5.0.1",
  "org.slf4j" % "slf4j-api" % "2.0.16",
  "org.specs2" %% "specs2-core" % "4.20.9" % Test,
  "mysql" % "mysql-connector-java" % "8.0.33",
  "javax.transaction" % "jta" % "1.1",
  "joda-time" % "joda-time" % "2.13.1",
  "org.joda" % "joda-convert" % "3.0.1",
  "com.typesafe" % "config" % "1.4.3",
  "com.datadoghq" % "java-dogstatsd-client" % "4.4.3",
)

fork := true

javaOptions ++= Seq(
  s"-Dpidfile.path=${File.createTempFile("piezoWorkerPid", null)}",
  s"-Dcom.lucidchart.piezo.heartbeatfile=${File.createTempFile("piezoHeartbeat", null)}",
  "-Dorg.quartz.properties=quartz.properties",
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xlint",
)

Compile / unmanagedClasspath += sourceDirectory.value / "run" / "resources"

version := sys.props.getOrElse("build.version", "0.0-SNAPSHOT")
