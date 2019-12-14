import com.lucidchart.sbtcross.{Axis, CrossableProject, DefaultAxis}
import com.typesafe.sbt.packager.archetypes.ServerLoader
import play.api.libs.json.Json

val distributionAxis = new DefaultAxis {
  def name = "dist"
  def major(version: String) = version
}

lazy val admin = project.dependsOn(worker_2_11).settings(scalaVersion := "2.11.8")
// there's probably a better way to also produce a Systemd deb
lazy val `admin-xenial` = admin.copy(id = "admin-xenial").settings(
  serverLoading in Debian := ServerLoader.Systemd,
  target := baseDirectory.value / "target-xenial",
  scalaVersion := "2.11.8"
)
lazy val `admin-bionic` = admin.copy(id = "admin-bionic").settings(
  serverLoading in Debian := ServerLoader.Systemd,
  target := baseDirectory.value / "target-bionic",
  scalaVersion := "2.11.8"
)


lazy val worker = project.cross
lazy val worker_2_11 = worker("2.11.12")
lazy val worker_2_12 = worker("2.12.8")

PgpKeys.pgpPassphrase in Global := Some(Array.emptyCharArray)

inThisBuild(Seq(
  credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD")),
  developers ++= List(
    Developer("disaacson", "Derrick Issacson", "", url("http://derrickisaacson.com/"))
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
  def files(deb: File, distribution: String) = Json.obj(
    "includePattern" -> baseDirectory.value.toPath.relativize(deb.toPath.normalize).toString,
    "matrixParams" -> Json.obj(
      "deb_architecture" -> "amd64,i386",
      "deb_component" -> "main",
      "deb_distribution" -> distribution
    ),
    "uploadPattern" -> s"pool/p/piezo-admin_${version.value}_${distribution}_all.deb"
  )
  val json = Json.obj(
    "files" -> Json.arr(
      files((packageBin in (admin, Debian)).value, "trusty"),
      files((packageBin in (`admin-xenial`, Debian)).value, "xenial"),
      files((packageBin in (`admin-bionic`, Debian)).value, "bionic")
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
