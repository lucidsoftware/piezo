import play.api.libs.json.Json

lazy val admin = project.dependsOn(worker)

lazy val worker = project

PgpKeys.pgpPassphrase in Global := Some(Array.emptyCharArray)

inThisBuild(Seq(
  credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD")),
  developers ++= List(
    Developer("disaacson", "Derrick Issacson", "", url("http://derrickisaacson.com/"))
  ),
  licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
  homepage := Some(url("https://github.com/lucidsoftware/piezo")),
  organization := "com.lucidchart",
  scalaVersion := "2.11.8",
  scmInfo := Some(ScmInfo(url("https://github.com/lucidsoftware/piezo"), "scm:git:git@github.com:lucidsoftware/piezo.git")),
  version := sys.props.getOrElse("build.version", "0-SNAPSHOT")
))

val bintrayDescriptor = taskKey[File]("Descriptor for TravisCI release to Bintray")

bintrayDescriptor in (ThisBuild, Debian) := {
  val json = Json.obj(
    "files" -> Json.arr(
      Json.obj(
        "includePattern" -> ".*/(.)(.*\\.deb)",
        "matrixParams" -> Json.obj(
          "deb_architecture" -> "amd64,i386",
          "deb_component" -> "main",
          "deb_distribution" -> "stable"
        ),
        "uploadPattern" -> "pool/stable/$1/$1$2"
      )
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
