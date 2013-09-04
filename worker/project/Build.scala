import sbt._
import sbt.Keys._

object Build extends Build {
  lazy val root =
    Project("root", file("."))

  val excludeFileRegex = """(.*)\.(properties|sql)$""".r
}