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
    "com.lucidchart" %% "piezo-worker" % "1.1-SNAPSHOT"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6"),
    scalaVersion := "2.10.1",
    resolvers ++= List(
      Resolver.file("local ivy repository", file(System.getenv("HOME") + "/.ivy2/local/"))(Resolver.ivyStylePatterns),
      "lucidchart release repository" at "http://repo.lucidchart.com:8081/artifactory/libs-release-local",
      "lucidchart external repository" at "http://repo.lucidchart.com:8081/artifactory/ext-release-local",
      "lucidchart snapshot repository" at "http://repo.lucidchart.com:8081/artifactory/libs-snapshot-local"
    )
  )

}
