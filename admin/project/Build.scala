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
    "org.quartz-scheduler" % "quartz" % "2.2.4-SNAPSHOT",
    "org.quartz-scheduler" % "quartz-jobs" % "2.2.3", // remove when this is fixed https://jira.terracotta.org/jira/browse/QTZ-404
    "com.lucidchart" %% "piezo-worker" % "1.13-SNAPSHOT"
  )

  val main = Project(appName, file("."))
    .enablePlugins(play.PlayScala)
    .settings(
      retrieveManaged := true,
      version := appVersion,
      libraryDependencies ++= appDependencies,
      javaOptions ++= Seq(
        s"-Dorg.quartz.properties=${baseDirectory.value / "conf/quartz.properties"}",
        s"-Dpidfile.path=/tmp/pid",
        s"-Dnetworkaddress.cache.ttl=10",
        s"-Dnetworkaddress.cache.negative.ttl=10"
      ),
      scalaVersion := "2.11.7",
      resolvers ++= List(
        Resolver.file("local ivy repository", file(System.getenv("HOME") + "/.ivy2/local/"))(Resolver.ivyStylePatterns),
        Resolver.mavenLocal,
        "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
        "Staging Sonatype repository" at "https://oss.sonatype.org/content/groups/staging/"
      )
    )
}
