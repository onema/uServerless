resolvers += "Onema Snapshots" at "s3://s3-us-east-1.amazonaws.com/ones-deployment-bucket/snapshots"

lazy val serverlessBaseRoot = (project in file("."))
.settings(
  organization := "onema",

  name := "serverless-base",

  version := "0.6.0-SNAPSHOT",

  scalaVersion := "2.12.5",

  libraryDependencies ++= {
    val awsSdkVersion = "1.11.313"
    Seq(
      // CORE!
       "onema"                      % "json-core_2.12"                      % "1.1.0",

      // AWS libraries
      "com.amazonaws"               % "aws-lambda-java-events"              % "2.1.0",
      "com.amazonaws"               % "aws-lambda-java-core"                % "1.2.0",
      "com.amazonaws"               % "aws-java-sdk-sns"                    % awsSdkVersion,
      "com.amazonaws"               % "aws-java-sdk-ssm"                    % awsSdkVersion,
      "com.amazonaws"               % "aws-java-sdk-dynamodb"               % awsSdkVersion,
      // The serverless java-container supports request context authorizer claims that are currently not available in the lambda-java-events
      "com.amazonaws.serverless"    % "aws-serverless-java-container-core"  % "0.9.1",

      // Http
      "org.apache.httpcomponents"   % "httpcore"                            % "4.4.8",

      // Logging
      "com.typesafe.scala-logging"  %% "scala-logging"                      % "3.7.2",
      "ch.qos.logback"              % "logback-classic"                     % "1.1.7",

      // Testing
      "org.scalatest"               % "scalatest_2.12"                      % "3.0.5"   % "test",
      "org.scalamock"               %% "scalamock"                          % "4.1.0"   % Test
    )
  }
)
//.dependsOn(jsonCore)

// Sub-projects
//lazy val jsonCore = RootProject(file("../JsonCore"))

publishMavenStyle := true
publishTo := Some("Onema Snapshots" at "s3://s3-us-east-1.amazonaws.com/ones-deployment-bucket/snapshots")
