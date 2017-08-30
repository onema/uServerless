import sbt.Keys._
import sbt._
import sbtrelease.Version

name := "ServerlessBase"

scalaVersion := "2.12.2"
releaseNextVersion := { ver => Version(ver).map(_.bumpMinor.string).getOrElse("Error") }
assemblyJarName in assembly := "serverlessbase.jar"

libraryDependencies ++= Seq(
  // JSON serializer
  "com.google.code.gson"      %   "gson"                    % "2.8.1",

  // AWS libraries
  "com.amazonaws"             %   "aws-lambda-java-events"  % "1.3.0",
  "com.amazonaws"             %   "aws-lambda-java-core"    % "1.1.0",
  "com.amazonaws.serverless"  %   "aws-serverless-java-container-core"  % "0.7",

  // Logging
  "com.typesafe.scala-logging" % "scala-logging_2.11"       % "3.5.0",
  "ch.qos.logback"             % "logback-classic"          % "1.1.7",

  // Testing
  "org.scalatest"             %   "scalatest_2.12"          % "3.0.4"   % "test",
  "org.scalamock"      % "scalamock-scalatest-support_2.12" % "3.6.0"   % "test"

)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")
