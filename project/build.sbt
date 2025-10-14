addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.6")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

libraryDependencies += "org.playframework" %% "play-json" % "3.0.4"

resolvers += Resolver.typesafeRepo("releases")
