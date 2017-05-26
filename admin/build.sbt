import play.api.libs.json.Json

enablePlugins(PlayScala)

bashScriptExtraDefines ++= Seq(
  "mkdir -m 664 /var/run/piezo-admin",
  s"chown ${(daemonUser in Linux).value} /var/run/piezo-admin",
  s"chown ${(daemonGroup in Linux).value} /var/run/piezo-admin",
  s"addJava -Dorg.quartz.properties=${defaultLinuxConfigLocation.value}/${(packageName in Linux).value}/quartz.properties",
  "addJava -Dpidfile.path=/var/run/piezo-admin/piezo-admin.pid",
  s"addJava -Dhttp.port=${PlayKeys.playDefaultPort.value}"
)

javaOptions += s"-Dorg.quartz.properties=${(resourceDirectory in Compile).value / "quartz.properties"}"

libraryDependencies ++= Seq(
  anorm,
  jdbc,
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "org.quartz-scheduler" % "quartz" % "2.1.7"
)

maintainer := "Lucid Software Team <ops@lucidchart.com>"

name := "piezo-admin"

packageDescription := "Piezo web admin"

PlayKeys.playDefaultPort := 8001
