import java.io.File

name := "piezo-worker"

Compile / mainClass := Some("com.lucidchart.piezo.Worker")

run / connectInput := true

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.7" % Provided,
  "org.quartz-scheduler" % "quartz" % "2.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.specs2" %% "specs2-core" % "4.5.1" % Test,
  "mysql" % "mysql-connector-java" % "8.0.32",
  "javax.transaction" % "jta" % "1.1",
  "joda-time" % "joda-time" % "2.8.1",
  "org.joda" % "joda-convert" % "1.7",
  "com.typesafe" % "config" % "1.0.0",
  "com.datadoghq" % "java-dogstatsd-client" % "4.1.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.6"
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

Compile / unmanagedClasspath += sourceDirectory.value / "run" / "resources"

version := sys.props.getOrElse("build.version", "0.0-SNAPSHOT")
