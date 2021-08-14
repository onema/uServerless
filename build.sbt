lazy val scala213 = "2.13.6"
lazy val scala212 = "2.12.14"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala213, scala212, scala211)

ThisBuild / organization := "io.onema"
ThisBuild / version      := "0.5.0"
ThisBuild / scalaVersion := scala213
ThisBuild / parallelExecution in Test := false

val awsSdkVersion = "1.11.1004"
val awsSdkV2Version = "2.17.19"

lazy val uServerless = (project in file("."))
.settings(skip in publish := true)
.aggregate(uServerlessEvents, uServerlessCore, uServerlessDynamoConfig, uServerlessSsmConfig, uServerlessTests)
publishArtifact in uServerless := false

lazy val uServerlessEvents = (project in file("userverless-events"))
.settings(
  name := "userverless-events",
  commonPublishSettings,
  crossScalaVersions := supportedScalaVersions
)

lazy val uServerlessCore = (project in file("userverless-core"))
.settings(
  name := "userverless-core",
  commonPublishSettings,
  crossScalaVersions := supportedScalaVersions,
  libraryDependencies ++= {
    Seq(
      // core libs
      "io.onema"                   %% "json-extensions"                     % "0.5.1",

      // AWS libraries
      "com.amazonaws"               % "aws-lambda-java-events"              % "3.8.0",
      "com.amazonaws"               % "aws-lambda-java-core"                % "1.2.1",
      // The serverless java-container supports request context authorizer claims that are currently not available in the lambda-java-events
      "com.amazonaws.serverless"    % "aws-serverless-java-container-core"  % "1.5.2",

      // Cats effect libraries
      "org.typelevel"               %% "cats-effect"                        % "3.2.2",

      // Logging
      "com.typesafe.scala-logging"  %% "scala-logging"                      % "3.9.2",
      "ch.qos.logback"              % "logback-classic"                     % "1.2.0",
      "net.logstash.logback"        % "logstash-logback-encoder"            % "5.3",
    )
  }
).dependsOn(uServerlessEvents)

lazy val uServerlessDynamoConfig = (project in file("userverless-dynamo-config"))
.settings(
  name := "userverless-dynamo-config",
  commonPublishSettings,
  crossScalaVersions := supportedScalaVersions,
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
  crossScalaVersions := supportedScalaVersions,
  libraryDependencies ++= {
    Seq(
      // AWS libraries
      "software.amazon.awssdk"               % "ssm"                    % awsSdkV2Version,
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
      "org.scalatest"             %% "scalatest"                 % "3.1.2"   % Test,
      "org.scalamock"             %% "scalamock"                 % "5.1.0"   % Test
    )
  },
).dependsOn(uServerlessEvents, uServerlessCore, uServerlessDynamoConfig, uServerlessSsmConfig)


// Maven Central Repo boilerplate config
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
