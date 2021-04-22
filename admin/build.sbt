enablePlugins(PlayScala)
enablePlugins(SystemdPlugin)

bashScriptExtraDefines ++= Seq(
  s"addJava -Dorg.quartz.properties=${defaultLinuxConfigLocation.value}/${(Linux / packageName).value}/quartz.properties",
  "addJava -Dpidfile.path=/run/piezo-admin/piezo-admin.pid",
  s"addJava -Dhttp.port=${PlayKeys.playDefaultPort.value}"
)

javaOptions += s"-Dorg.quartz.properties=${(Compile / resourceDirectory).value / "quartz.properties"}"

libraryDependencies ++= Seq(
  jdbc,
  "org.ow2.asm" % "asm" % "8.0.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.quartz-scheduler" % "quartz" % "2.1.7",
  "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided",
   specs2 % Test
)

maintainer := "Lucid Software, Inc. <ops@lucidchart.com>"

name := "piezo-admin"

packageDescription := "Piezo web admin"

PlayKeys.playDefaultPort := 8001

Debian/defaultLinuxStartScriptLocation  := "/lib/systemd/system"

publishTo := sonatypePublishToBundle.value
