enablePlugins(PlayScala)
enablePlugins(SystemdPlugin)

import play.sbt.routes.RoutesKeys

bashScriptExtraDefines ++= Seq(
  s"addJava -Dorg.quartz.properties=${defaultLinuxConfigLocation.value}/${(Linux / packageName).value}/quartz.properties",
  "addJava -Dpidfile.path=/run/piezo-admin/piezo-admin.pid",
  s"addJava -Dhttp.port=${PlayKeys.playDefaultPort.value}",
)

// Workaround for https://github.com/playframework/playframework/issues/7382
// so we don't get unused import warnings
RoutesKeys.routesImport := Seq.empty
// templateImports := Seq.empty

javaOptions += s"-Dorg.quartz.properties=${(Compile / resourceDirectory).value / "quartz.properties"}"

libraryDependencies ++= Seq(
  jdbc,
  "org.ow2.asm" % "asm" % "8.0.1",
  "ch.qos.logback" % "logback-classic" % "1.5.16",
  "org.quartz-scheduler" % "quartz" % "2.5.0",
  "org.quartz-scheduler" % "quartz-jobs" % "2.5.0",
  "com.softwaremill.macwire" %% "macros" % "2.6.6" % "provided",
  specs2 % Test,
)

scalacOptions ++= Seq(
  "-Wconf:src=.*html&msg=unused import:s",
)

Universal / doc / sources := Seq.empty
Debian / doc / sources := Seq.empty

Debian / version := {
  val noDashVersion = (Compile / version).value.replace("-", "~")
  if (noDashVersion.matches("^\\d.*")) {
    noDashVersion
  } else {
    "0~" + noDashVersion
  }
}

maintainer := "Lucid Software, Inc. <ops@lucidchart.com>"

name := "piezo-admin"

packageDescription := "Piezo web admin"

PlayKeys.playDefaultPort := 8001

Debian / defaultLinuxStartScriptLocation := "/lib/systemd/system"
