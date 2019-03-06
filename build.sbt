ThisBuild / organization := "io.onema"
ThisBuild / version      := "0.4.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / parallelExecution in Test := false

val awsSdkVersion = "1.11.510"
val awsSdkV2Version = "2.3.9"

lazy val uServerless = (project in file("."))
.settings(skip in publish := true)
.aggregate(uServerlessEvents, uServerlessCore, uServerlessDynamoConfig, uServerlessSsmConfig, uServerlessTests)
publishArtifact in uServerless := false

lazy val uServerlessEvents = (project in file("userverless-events"))
.settings(
  name := "userverless-events",
  commonPublishSettings
)

lazy val uServerlessCore = (project in file("userverless-core"))
.settings(
  name := "userverless-core",
  commonPublishSettings,
  libraryDependencies ++= {
    Seq(
      // core libs
      "io.onema"                   % "json-extensions_2.12"                % "0.5.0",

      // AWS libraries
      "com.amazonaws"               % "aws-lambda-java-events"              % "2.2.5",
      "com.amazonaws"               % "aws-lambda-java-core"                % "1.2.0",
      // The serverless java-container supports request context authorizer claims that are currently not available in the lambda-java-events
      "com.amazonaws.serverless"    % "aws-serverless-java-container-core"  % "0.9.1",

      // Logging
      "com.typesafe.scala-logging"  %% "scala-logging"                      % "3.9.0",
      "ch.qos.logback"              % "logback-classic"                     % "1.2.0",
      "net.logstash.logback"        % "logstash-logback-encoder"            % "5.3",
    )
  }
).dependsOn(uServerlessEvents)

lazy val uServerlessDynamoConfig = (project in file("userverless-dynamo-config"))
.settings(
  name := "userverless-dynamo-config",
  commonPublishSettings,
  libraryDependencies ++= {
    Seq(
      // AWS libraries
      "com.amazonaws"               % "aws-java-sdk-dynamodb"               % awsSdkVersion,
    )
  }
).dependsOn(uServerlessCore)

lazy val uServerlessSsmConfig = (project in file("userverless-ssm-config"))
.settings(
  name := "userverless-ssm-config",
  commonPublishSettings,
  libraryDependencies ++= {
    Seq(
      // AWS libraries
      "com.amazonaws"               % "aws-java-sdk-ssm"                    % awsSdkVersion,
    )
  }
).dependsOn(uServerlessCore)


lazy val uServerlessTests = (project in file("userverless-tests"))
.settings(
  name := "userverless-tests",
  publishTo := Some(Resolver.file("unused repo", file("foo/bar"))),
  publishArtifact := false,
  libraryDependencies ++= {
    Seq(
      // Testing
      "org.scalatest"               % "scalatest_2.12"                      % "3.0.5"   % Test,
      "org.scalamock"               %% "scalamock"                          % "4.1.0"   % Test
    )
  },
  publishArtifact := false
).dependsOn(uServerlessEvents, uServerlessCore, uServerlessDynamoConfig, uServerlessSsmConfig)


// Maven Central Repo boilerplate configuration
lazy val commonPublishSettings = Seq(
//  publishTo := Some("Onema Snapshots" at "s3://s3-us-east-1.amazonaws.com/ones-deployment-bucket/snapshots"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/onema/uServerless")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/onema/uServerless"),
      "scm:git@github.com:onema/uServerless.git"
    )
  ),
  developers := List(
    Developer(
      id    = "onema",
      name  = "Juan Manuel Torres",
      email = "software@onema.io",
      url   = url("https://github.com/onema/")
    )
  ),
  publishArtifact in Test := false
)
