import play.Play.autoImport._
import sbt.Keys._
import sbt._
import sbt.plugins._

object ApplicationBuild extends Build {

  val appName         = "admin"
  val appVersion      = "1.1"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.quartz-scheduler" % "quartz" % "2.1.7",
    "com.lucidchart" %% "piezo-worker" % "1.11-SNAPSHOT"
  )


  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    scalaVersion := "2.11.7",
    resolvers ++= List(
      Resolver.file("local ivy repository", file(System.getenv("HOME") + "/.ivy2/local/"))(Resolver.ivyStylePatterns),
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Staging Sonatype repository" at "https://oss.sonatype.org/content/groups/staging/"
    )
  )

}
