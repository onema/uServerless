µServerless for Scala
=====================
[![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiQnkzdWlQWXJ0cWtDTjl5M2J3OGJJYTRtK01vVXJMazBRanhidXdUbkVUejI4Y1g4WUhoK3k4cWRUV1NSOUl4RzJ0VFMvSy9Xb3BYTGN6eUUyM2ZsMlRZPSIsIml2UGFyYW1ldGVyU3BlYyI6IlFrTncvSHplNTRYUk1iRjciLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)](https://console.aws.amazon.com/codebuild/home?region=us-east-1#/projects/uServerless/view)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/634b14e124ec44429b1b81fdbcb6548f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/UServerless&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/634b14e124ec44429b1b81fdbcb6548f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/UServerless&amp;utm_campaign=Badge_Coverage)
[![LICENSE](https://img.shields.io/badge/license-Apache--2.0-blue.svg?longCache=true&style=flat-square)](LICENSE)

The µServerless package (pronounced micro-serverless) is a small collection of classes, traits and adapters to help you
build distributed applications using AWS Lambda and scala. 

µServerless has the following features:

1. Handles exceptions and exception handlers to take actions when exception occur
1. API Gateway has a special handler that to help you deal with Lambda Proxy Request and Responses
1. SNS Handler that unpacks and deserialize the message
1. Provides a trait to get configuration values or secrets from SSM Parameter Store 
1. Structured logging
1. Error and Metric reporting is possible using the **[Overwatch](https://github.com/onema/uServerlessOverwatch)** app. 

> [Overwatch](https://github.com/onema/uServerlessOverwatch) this is a metric and error reporting application that configures 
general purpose infrastructure for all µServerless applications

## Install
Add the following to your SBT config:

```
libraryDependencies += "io.onema" %% "userverless-core" % "<LATEST_VERSION>"
```

