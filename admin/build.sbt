lazy val piezoAdmin = (project in file(".")).enablePlugins(PlayScala)

bashScriptExtraDefines ++= Seq(
  "mkdir -m 664 /var/run/piezo-admin/",
  s"chown ${(daemonUser in Linux).value} /var/run/piezo/admin",
  s"chown ${(daemonGroup in Linux).value} /var/run/piezo/admin",
  """JAVA_OPTS="-Dpidfile.path=/var/run/piezo-admin/piezo-admin.pid $JAVA_OPTS"""",
  s"""JAVA_OPTS="-Dhttp.port=${PlayKeys.playDefaultPort.value} $$JAVA_OPTS""""
)

bashScriptEnvConfigLocation := Some("/etc/piezo-admin/conf")

bashScriptConfigLocation := bashScriptEnvConfigLocation.value // it seems to me a bug that both must be specified

daemonGroup in Linux := "nobody"

daemonUser in Linux := "nobody"

libraryDependencies ++= Seq(
  anorm,
  jdbc,
  "com.lucidchart" %% "piezo-worker" % "1.11",
  "org.quartz-scheduler" % "quartz" % "2.1.7"
)

linuxEtcDefaultTemplate := url(s"file://${baseDirectory.value}/templates/conf")

maintainer := "Lucid Software Team <ops@lucidchart.com>"

name := "piezo-admin"

organization := "com.lucidchart"

packageDescription := "Piezo web admin"

PlayKeys.playDefaultPort := 11001

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Sonatype staging repository" at "https://oss.sonatype.org/content/groups/staging/"
)

scalaVersion := "2.11.7"

version := "1.1"
