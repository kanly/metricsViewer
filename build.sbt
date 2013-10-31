import org.vaadin.sbt.VaadinPlugin._

name := "metricsViewer"

version in ThisBuild := "0.2"

organization in ThisBuild := "it.posteitaliane.omp"

scalaVersion := "2.10.3"

crossPaths in ThisBuild := false

libraryDependencies ++= Seq( 
    "com.typesafe.akka" % "akka-actor_2.10" % "2.2.0",
    "com.vaadin" % "vaadin-server" % vaadinVersion,
    "com.vaadin" % "vaadin-client-compiled" % vaadinVersion,
    "com.vaadin" % "vaadin-client" % vaadinVersion,
    "com.vaadin" % "vaadin-client-compiler" % vaadinVersion,
    "com.vaadin" % "vaadin-push" % vaadinVersion,
    "com.vaadin" % "vaadin-themes" % vaadinVersion,
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided", 
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.2.1",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2",
    "org.neo4j" % "neo4j" % "2.0.0-M03",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "com.typesafe" % "scalalogging-slf4j_2.10" % "1.0.1",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "com.typesafe.akka" % "akka-testkit_2.10" % "2.1.2",
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container",
    "org.eclipse.jetty" % "jetty-jsp" % jettyVersion % "container",
    "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % "container",
    "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion % "container"
)

val vaadinVersion = "7.1.6"
val jettyVersion = "9.0.5.v20130815"

vaadinWidgetsets := Seq("it.posteitaliane.omp.UI.AppWidgetSet")

javaOptions in compileVaadinWidgetsets := Seq("-Xss8M", "-Xmx512M", "-XX:MaxPermSize=512M")

vaadinOptions in compileVaadinWidgetsets := Seq("-strict", "-draftCompile")

javaOptions in vaadinDevMode ++= Seq("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")

vaadinWebSettings
