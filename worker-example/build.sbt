lazy val exampleWorker = (project in file(".")).dependsOn(piezoWorker)

lazy val piezoWorker = RootProject(file("../worker"))

autoScalaLibrary := false

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "mysql" % "mysql-connector-java" % "5.1.25"
)

fork := true

javaOptions ++= Seq(
  s"-Dpidfile.path=/tmp/${name.value}.pid"
)

mainClass in Compile := Some("com.lucidchart.piezo.Worker")

scalaVersion := "2.11.7"
z