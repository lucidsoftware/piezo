addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")

addSbtPlugin("com.lucidchart" % "sbt-cross" % "4.0")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.6")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")

libraryDependencies += "org.playframework" %% "play-json" % "3.0.4"

resolvers += Resolver.typesafeRepo("releases")
