import com.lucidchart.sbtcross.{Axis, CrossableProject, DefaultAxis}
import play.api.libs.json.Json

lazy val admin = project.dependsOn(worker_2_13).settings(scalaVersion := "2.13.16")

lazy val commonSettings = Seq(publishTo := sonatypePublishToBundle.value)

lazy val worker = project.cross
lazy val worker_2_12 = worker("2.12.20").settings(commonSettings)
lazy val worker_2_13 = worker("2.13.16").settings(commonSettings)

PgpKeys.pgpPassphrase in Global := Some(Array.emptyCharArray)

inThisBuild(
  Seq(
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
    scalaVersion := "2.13.16",
    scmInfo := Some(
      ScmInfo(url("https://github.com/lucidsoftware/piezo"), "scm:git:git@github.com:lucidsoftware/piezo.git"),
    ),
    version := sys.props.getOrElse("build.version", "0-SNAPSHOT"),
    versionScheme := Some("early-semver"),
  ),
)

publishTo := sonatypePublishToBundle.value
