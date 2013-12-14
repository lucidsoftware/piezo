import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "admin"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.quartz-scheduler" % "quartz" % "2.1.7",
    "com.lucidchart" %% "piezo-worker" % "1.3-SNAPSHOT"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6"),
    scalaVersion := "2.10.1",
    resolvers ++= List(
      Resolver.file("local ivy repository", file(System.getenv("HOME") + "/.ivy2/local/"))(Resolver.ivyStylePatterns),
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Staging Sonatype repository" at "https://oss.sonatype.org/content/groups/staging/"
    )
  )

}
