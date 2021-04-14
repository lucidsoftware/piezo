addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")

addSbtPlugin("com.lucidchart" % "sbt-cross" % "4.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"

resolvers += Resolver.typesafeRepo("releases")
