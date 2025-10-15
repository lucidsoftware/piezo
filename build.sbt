import play.api.libs.json.Json

inThisBuild(
  Seq(
    scalaVersion := "3.3.4",
    developers ++= List(
      Developer("lucidsoftware", "Lucid Software, Inc.", "", url("https://lucid.co/")),
    ),
    licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    homepage := Some(url("https://github.com/lucidsoftware/piezo")),
    organization := "com.lucidchart",
    scmInfo := Some(
      ScmInfo(url("https://github.com/lucidsoftware/piezo"), "scm:git:git@github.com:lucidsoftware/piezo.git"),
    ),
    versionScheme := Some("early-semver"),
    scalacOptions ++= Seq(
      "-no-indent",
      "-Wunused:linted",
      "-Werror",
      // "-Xlint",
    ),
  ),
)

lazy val admin = project.dependsOn(worker)

lazy val worker = project

PgpKeys.pgpPassphrase in Global := Some(Array.emptyCharArray)
