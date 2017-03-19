import sbtassembly.AssemblyPlugin.autoImport._

name := "agoda-assignment"
version := "1.0"
scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("hseeberger", "maven"))

libraryDependencies ++= {
  val version = new {
    val akka            = "2.4.10"
    val json4s          = "3.3.0"
    val akkaHttpJson    = "1.10.0"
    val scalatest       = "2.2.1"
    val logback         = "1.1.3"
    val scalaLogging    = "3.1.0"
  }

  Seq(
    "com.typesafe.akka"   %% "akka-actor"               % version.akka,
    "com.typesafe.akka"   %% "akka-stream"              % version.akka,
    "com.typesafe.akka"   %% "akka-testkit"             % version.akka        % "test",
    ("com.typesafe.akka"  %% "akka-slf4j"               % version.akka).exclude("org.slf4j", "slf4j-api"),

    "com.typesafe.akka"   %% "akka-http-experimental"   % version.akka,
    "com.typesafe.akka"   %% "akka-http-core"           % version.akka,
    "com.typesafe.akka"   %% "akka-http-testkit"        % version.akka        % "test",

    "org.json4s"          %% "json4s-native"            % version.json4s,
    "org.json4s"          %% "json4s-jackson"           % version.json4s,
    "de.heikoseeberger"   %% "akka-http-json4s"         % version.akkaHttpJson,

    "org.scalatest"       %% "scalatest"                % version.scalatest   % "test",

    ("ch.qos.logback"     %  "logback-classic"          % version.logback).exclude("org.slf4j", "slf4j-api"),
    "com.typesafe.scala-logging" %% "scala-logging"     % version.scalaLogging
  )
}

test in assembly := {}