import com.typesafe.sbt.SbtStartScript 

import com.earldouglas.xsbtwebplugin.PluginKeys

name := "SPIRIT-News"

version := "1.2"

organization := "SPIRIT"

scalaVersion := "2.10.2"

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases" at "http://oss.sonatype.org/content/repositories/releases"
                )

seq(webSettings :_*)

seq(SbtStartScript.startScriptForClassesSettings: _*)

// net.virtualvoid.sbt.graph.Plugin.graphSettings // for sbt dependency-graph plugin

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature","-language:implicitConversions","-language:postfixOps")

libraryDependencies ++= {
  val liftVersion = "2.5"
  val twitter4jVersion="3.0.3"
  val dispatchVersion="0.8.10"
  Seq(
   "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7",
    "org.twitter4j" % "twitter4j-stream" % twitter4jVersion, 
    "net.databinder" % "dispatch-core_2.10" % dispatchVersion withSources,
    "net.databinder" % "dispatch-http_2.10" % dispatchVersion withSources,
    "net.databinder" % "dispatch-oauth_2.10" % dispatchVersion withSources,
    "net.liftmodules" %% "textile_2.5" % "1.3" % "compile->default" withSources,
    "net.liftmodules" %% "widgets_2.5" % "1.3" % "compile->default" withSources,
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-mongodb" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-record" % liftVersion % "compile->default" withSources,
    "net.liftweb" %% "lift-json" % liftVersion % "compile->default" withSources,
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test->default",
    "org.mockito" % "mockito-all" % "1.8.0" % "test",
    "org.mortbay.jetty" % "jetty" % "6.1.26" % "test->default",
    "ch.qos.logback" % "logback-classic" % "1.0.11",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "compile,container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "javax.servlet" % "servlet-api" % "2.5" % "provided",
    "org.scala-stm" %% "scala-stm" % "0.7"
    )
}

    buildInfoSettings

    sourceGenerators in Compile <+= buildInfo

    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

    buildInfoPackage := "org.unsane.spirit.news.model"