import com.lucidchart.sbtcross.{Axis, CrossableProject, DefaultAxis}
import com.typesafe.sbt.packager.archetypes.ServerLoader
import play.api.libs.json.Json

lazy val admin = project.dependsOn(worker_2_11).settings(
    serverLoading in Debian := ServerLoader.Systemd,
    scalaVersion := "2.11.8"
)

lazy val commonSettings = Seq(publishTo := sonatypePublishToBundle.value)

lazy val worker = project.cross
lazy val worker_2_11 = worker("2.11.12").settings(commonSettings)
lazy val worker_2_12 = worker("2.12.12").settings(commonSettings)
lazy val worker_2_13 = worker("2.13.2").settings(commonSettings)

PgpKeys.pgpPassphrase in Global := Some(Array.emptyCharArray)

inThisBuild(Seq(
  credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD")),
  developers ++= List(
    Developer("lucidsoftware", "Lucid Software, Inc.", "", url("https://lucid.co/"))
  ),
  licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  homepage := Some(url("https://github.com/lucidsoftware/piezo")),
  organization := "com.lucidchart",
  scalaVersion := "2.11.12",
  scmInfo := Some(ScmInfo(url("https://github.com/lucidsoftware/piezo"), "scm:git:git@github.com:lucidsoftware/piezo.git")),
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT")
))

val bintrayDescriptor = taskKey[File]("Descriptor for TravisCI release to Bintray")

bintrayDescriptor in (ThisBuild, Debian) := {
  def files(deb: File) = Json.obj(
    "includePattern" -> baseDirectory.value.toPath.relativize(deb.toPath.normalize).toString,
    "matrixParams" -> Json.obj(
      "deb_architecture" -> "amd64,i386",
      "deb_component" -> "main",
      "deb_distribution" -> "piezo"
    ),
    "uploadPattern" -> s"pool/p/piezo-admin_${version.value}_all.deb"
  )
  val json = Json.obj(
    "files" -> Json.arr(
      files((packageBin in (admin, Debian)).value)
    ),
    "package" -> Json.obj(
      "name" -> "piezo",
      "repo" -> "apt",
      "subject" -> "lucidsoftware"
    ),
    "publish" -> true,
    "version" -> Json.obj(
      "name" -> version.value,
      "gpgSign" -> true,
      "vcs_tag" -> version.value
    )
  )
  val file = target.value / Debian.name / "bintray.json"
  file.getParentFile.mkdirs()
  IO.write(file, Json.prettyPrint(json))
  file
}

publishTo := sonatypePublishToBundle.value
