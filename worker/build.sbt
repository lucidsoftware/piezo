name := "piezo-worker"

organization := "com.lucidchart"

version := "1.4"

scalaVersion := "2.10.1"

exportJars := true

exportJars in Test := false

autoScalaLibrary := true

retrieveManaged := true

mainClass := Some("com.lucidchart.piezo.Worker")

libraryDependencies ++= Seq(
	"org.specs2" %% "specs2" % "1.14" % "test",
	"ch.qos.logback" % "logback-classic" % "1.0.1",
	"org.quartz-scheduler" % "quartz" % "2.1.7",
	"mysql" % "mysql-connector-java" % "5.1.25",
	"javax.transaction" % "jta" % "1.1",
	"net.databinder" %% "dispatch-http" % "0.8.8",
	"com.typesafe" % "config" % "1.0.0",
	"net.liftweb" %% "lift-json" % "2.5.1",
	"com.typesafe.akka" %% "akka-actor" % "2.1.0"
)

resolvers ++= List(
	DefaultMavenRepository,
	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
	"Staging Sonatype repository" at "https://oss.sonatype.org/content/groups/staging/"
)

TaskKey[Set[File]]("stage") <<= (fullClasspath in Runtime, target) map { (cp, out) =>
  val entries: Seq[File] = cp.files
  val toDirectory: File = out / "staged"
  IO.copy( entries x flat(toDirectory) )
}

mappings in (Compile, packageBin) ~= { (ms: Seq[(File, String)]) =>
  ms filter {
    case (file, toPath) =>
      !excludeFileRegex.pattern.matcher(file.getName).matches
  }
}

pomExtra := (
  <url>http://jsuereth.com/scala-arm</url>
  <licenses>
    <license>
      <name>Apache License</name>
      <url>http://www.apache.org/licenses/</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
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

useGpg := true

pgpReadOnly := false

publishMavenStyle := true

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
