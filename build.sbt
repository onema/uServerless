resolvers += "Onema Snapshots" at "s3://s3-us-east-1.amazonaws.com/ones-deployment-bucket/snapshots"

val coreVersion = "1.0.0"

lazy val serverlessBaseRoot = (project in file("."))
.settings(
  organization := "onema",

  name := "serverless-base",

  version := "0.3.0",

  scalaVersion := "2.12.4",

  libraryDependencies ++= {
    Seq(
      // CORE!
       "onema"                      % "json-core_2.12"                % coreVersion,

      // AWS libraries
      "com.amazonaws" % "aws-lambda-java-events" % "2.0.2",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
      "com.amazonaws" % "aws-java-sdk-sns" % "1.11.263",
      "com.amazonaws.serverless" % "aws-serverless-java-container-core" % "0.8",

      // Http
      "org.apache.httpcomponents" % "httpcore" % "4.4.8",

      // Logging
      "com.typesafe.scala-logging" %% "scala-logging"           % "3.7.2",
      "ch.qos.logback"             % "logback-classic"          % "1.1.7",

      // Testing
      "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test",
      "org.scalamock" % "scalamock-scalatest-support_2.12" % "3.6.0" % "test"
    )
  }
)
//.dependsOn(jsonCore)

// Sub-projects
//lazy val jsonCore = RootProject(file("../JsonCore"))

publishMavenStyle := true
publishTo := Some("Onema Snapshots" at "s3://s3-us-east-1.amazonaws.com/ones-deployment-bucket/snapshots")
