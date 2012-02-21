import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val mavenLocal = "Local Maven Repository" at
  "file://"+Path.userHome+"/.m2/repository"

  val scalatools_release = "Scala Tools Releases" at
  "http://scala-tools.org/repo-releases"
  val liftVersion = "2.4"

  override def libraryDependencies = Set(
    "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7",
    "net.databinder" %% "dispatch" % "0.7.7",
    "net.liftweb" %% "lift-textile" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-mongodb" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-record" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-json" % liftVersion % "compile->default" withSources,
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" %% "specs" % "1.6.7" % "test->default",
    "org.mockito" % "mockito-all" % "1.8.0" % "test",
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default",
    "ch.qos.logback" % "logback-classic" % "0.9.26"
  ) ++ super.libraryDependencies
}

