import play.api.libs.json.Json

lazy val admin = project.dependsOn(worker)

lazy val commonSettings = Seq(publishTo := sonatypePublishToBundle.value)

lazy val worker = project.settings(publishTo := sonatypePublishToBundle.value)

PgpKeys.pgpPassphrase in Global := Some(Array.emptyCharArray)

inThisBuild(
  Seq(
    scalaVersion := "3.3.4",
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      System.getenv("SONATYPE_USERNAME"),
      System.getenv("SONATYPE_PASSWORD"),
    ),
    developers ++= List(
      Developer("lucidsoftware", "Lucid Software, Inc.", "", url("https://lucid.co/")),
    ),
    licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    homepage := Some(url("https://github.com/lucidsoftware/piezo")),
    organization := "com.lucidchart",
    scmInfo := Some(
      ScmInfo(url("https://github.com/lucidsoftware/piezo"), "scm:git:git@github.com:lucidsoftware/piezo.git"),
    ),
    version := sys.props.getOrElse("build.version", "0-SNAPSHOT"),
    versionScheme := Some("early-semver"),
    scalacOptions ++= Seq(
      "-no-indent",
      "-Wunused:linted",
      "-Werror",
      // "-Xlint",
    ),
  ),
)

publishTo := sonatypePublishToBundle.value
