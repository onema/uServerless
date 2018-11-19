µServerless for Scala
=====================
[![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiQnkzdWlQWXJ0cWtDTjl5M2J3OGJJYTRtK01vVXJMazBRanhidXdUbkVUejI4Y1g4WUhoK3k4cWRUV1NSOUl4RzJ0VFMvSy9Xb3BYTGN6eUUyM2ZsMlRZPSIsIml2UGFyYW1ldGVyU3BlYyI6IlFrTncvSHplNTRYUk1iRjciLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)](https://console.aws.amazon.com/codebuild/home?region=us-east-1#/projects/uServerless/view)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/634b14e124ec44429b1b81fdbcb6548f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/UServerless&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/634b14e124ec44429b1b81fdbcb6548f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/UServerless&amp;utm_campaign=Badge_Coverage)
[![LICENSE](https://img.shields.io/badge/license-Apache--2.0-blue.svg?longCache=true&style=flat-square)](LICENSE)

The µServerless package (pronounced micro-serverless) is a small collection of classes, traits and adapters to help you
build AWS Lambda functions using scala. 

## Table of Content
<!-- TOC -->

- [Table of Content](#table-of-content)
- [Install](#install)
- [Invocation model](#invocation-model)
- [Lambda Configuration](#lambda-configuration)
- [Handlers available](#handlers-available)
    - [Lambda Handler Usage](#lambda-handler-usage)
        - [Simple lambda function with Unit return type](#simple-lambda-function-with-unit-return-type)
        - [Simple lambda function with a custom return type](#simple-lambda-function-with-a-custom-return-type)
    - [SNS Handler](#sns-handler)
    - [API Gateway Handler](#api-gateway-handler)
        - [Handling a valid requests](#handling-a-valid-requests)
        - [Handling unexpected errors for API Gateway](#handling-unexpected-errors-for-api-gateway)
        - [Enable CORS](#enable-cors)
        - [`EnvCorsConfiguration`](#envcorsconfiguration)
        - [`DynamodbCorsConfiguration`](#dynamodbcorsconfiguration)
        - [`SsmCorsConfiguration`](#ssmcorsconfiguration)
        - [`NoopCorsConfiguration`](#noopcorsconfiguration)
        - [Example](#example)
- [Dealing with exceptions](#dealing-with-exceptions)
    - [Enable error notifications](#enable-error-notifications)
- [Keeping functions warm](#keeping-functions-warm)
- [Event listeners](#event-listeners)
    - [Validation listener](#validation-listener)
    - [Response listener](#response-listener)

<!-- /TOC -->

## Install
Add the following to your SBT config:

```
libraryDependencies += "io.onema" %% "userverless-core" % "<LATEST_VERSION>"
```

## Invocation model
The adapters have the following properties:

1. Enable you to separate the lambda `Function` handler from the `Logic` of the application
  - The `Function` is responsible for building all the dependencies and constructing the logic object and handling errors, notifications and keeping the function warm (if enabled)
  - The `Logic` should acquire all it's dependencies via DI, hence it should be the testable part of your code
1. Handles exceptions and exception handlers to take actions when exception occur
1. API Gateway has a special handler that to help you deal with Lambda Proxy Request and Responses
1. SNS Handler that unpacks and deserialize the message
1. Provides a trait to get configuration values or secrets from SSM Parameter Store 
1. Works with the **Overwatch** app, this is a metric and error reporting application that configures general purpose infrastructure for all µServerless applications

## Keeping functions warm
Functions can be kept warm by adding a `schedule` event to the functions with the following input:

```yaml

functions:
  success:
    handler: serverless.Function::lambdaHandler
    events:
      # Main trigger
      - sns: some-tpic
      
      # Custom event to keep the function warm
      - schedule:
          rate: rate(5 minutes)
          input:
            warmup: true
```
