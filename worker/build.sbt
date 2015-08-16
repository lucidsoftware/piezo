name := "piezo-worker"

organization := "com.lucidchart"

version := "1.11"

scalaVersion := "2.11.7"

crossScalaVersions += "2.10.5"

homepage := Some(url("https://github.com/lucidsoftware/piezo"))

mainClass := Some("com.lucidchart.piezo.Worker")

libraryDependencies ++= Seq(
  "org.quartz-scheduler" % "quartz" % "2.1.7",
  "org.specs2" %% "specs2" % "2.3.13" % Test,
  "joda-time" % "joda-time" % "2.8.1",
  "org.joda" % "joda-convert" % "1.7",
  "org.slf4j" % "slf4j-api" % "1.7.12",
	"com.typesafe" % "config" % "1.0.0"
) ++ (CrossVersion.binaryScalaVersion(scalaVersion.value) match {
  case "2.11" => Seq("org.scala-lang" % "scala-actors" % scalaVersion.value)
  case _ => Seq()
})

licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

resolvers ++= Seq(
	DefaultMavenRepository,
  Resolver.typesafeRepo("releases")
)

pomExtra := (
  <scm>
    <url>git@github.com:lucidsoftware/piezo.git</url>
    <connection>scm:git:git@github.com:lucidsoftware/piezo.git</connection>
  </scm>
  <developers>
    <developer>
      <id>disaacson</id>
      <name>Derrick Isaacson</name>
      <url>http://derrickisaacson.com/</url>
    </developer>
    <developer>
      <id>draperp</id>
      <name>Paul Draper</name>
      <url>http://about.me/pauldraper</url>
    </developer>
  </developers>
)

pomIncludeRepository := { _ => false }

pgpPassphrase := Some(Array())

pgpPublicRing := file(System.getProperty("user.home")) / ".pgp" / "pubring"

pgpSecretRing := file(System.getProperty("user.home")) / ".pgp" / "secring"

publishMavenStyle := true

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xlint"
)
