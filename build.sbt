import sbt.url

lazy val serverlessBaseRoot = (project in file("."))
.settings(
  organization := "io.onema",

  name := "userverless",

  version := "0.1.0",

  scalaVersion := "2.12.6",

  libraryDependencies ++= {
    val awsSdkVersion = "1.11.380"
    Seq(
      // CORE!
       "io.onema"                   % "json-extensions_2.12"                % "0.3.0",

    // AWS libraries
      "com.amazonaws"               % "aws-lambda-java-events"              % "2.2.2",
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
      "org.scalatest"               % "scalatest_2.12"                      % "3.0.5"   % Test,
      "org.scalamock"               %% "scalamock"                          % "4.1.0"   % Test
    )
  }
)
//.dependsOn(jsonExtensions)

// Sub-projects
//lazy val jsonExtensions = RootProject(file("../JsonExtensions"))
// Maven Central Repo boilerplate configuration
pomIncludeRepository := { _ => false }
licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/onema/uServerless"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/onema/uServerless"),
    "scm:git@github.com:onema/uServerless.git"
  )
)
developers := List(
  Developer(
    id    = "onema",
    name  = "Juan Manuel Torres",
    email = "software@onema.io",
    url   = url("https://github.com/onema/")
  )
)
publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
publishArtifact in Test := false
parallelExecution in Test := false
