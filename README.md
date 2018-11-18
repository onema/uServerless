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

## Lambda Configuration
µServerless supports three methods to get configuration values: environment variables, SSM Parameter Store, and Noop

By default the lambda handlers require you to implement the methods defined in the `LambdaConfiguration` trait. 
You can satisfy this requirement by simply extending your function with one of the provided traits: 
* `SsmLambdaConfig`
* `EnvLambdaConfig`
* `NoopLambdaConfig`

`Function`s can retrieve configuration values using the following methods:
* `getValue(name: String): Option[String]`: Name is the fully qualified name and includes the hierarchy of the parameter path and name. For the env vars a value like `/database/password` will be converted to `DATABASE_PASSWORD` 
* `getValues(path: String): Map[String, String]`: Name is a partial path. This will return a map of values containing key -> values for all the elements in the hierarchy. For name like `/database` the method may return all the database related values such as `/database/username`, `/database/password`, etc.

SSM Parameter Store could do a lookup of value names prepending an "environment name" e.g. prod, staging, dev. This is use to be able to create parameters with unique names and reference
them in the function using a consistent name. For example the value `/database/username` for the environment name `prod` will result in a lookup of the parameter `/prod/database/username`.
The environment name is set as the environment variable `STAGE_NAME`.

SSM Parameter store configuration must be installed using the `userverless-ssm-config` package:
```
libraryDependencies += "io.onema" %% "userverless-ssm-config" % "<LATEST_VERSION>"
```

To use the SSM environment variable in  your function simply extend from the `SsmLambdaConfiguration` trait like such

```scala
class TestFunction(snsClientMock: AmazonSNSAsync) extends LambdaHandler with SsmLambdaConfiguration {
  val foo: Option[String] = getValue("/foo")
  val barHierarchy: Map[String, String] = getValues("/bar/")
}
```

## Handlers available
There are a few traits that can be used in your functions:
* LambdaHandler: A generic handler that uses a type parameters to defined the handler event and return type. 
* ApiGatewayHandler: An APIGateway specific handler that generates an API Proxy request. 
* SnsHandler: A handler that unpacks the internal message to the specified type. 
It has support methods to deal with java input and output streams; these are used when using apis that have a cognito custom authorizer.

### Lambda Handler Usage

#### Simple lambda function with Unit return type
```scala

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import org.apache.http.HttpStatus

object Logic {
  val log = Logger("logic")
  def handleRequest(message: String): Unit = {
    log.info(message) 
  }
}

class Function extends LambdaHandler[SNSEvent, Unit] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(snsEvent: SNSEvent, context: Context): Unit = {
    val message = snsEvent.getRecords.get(0).getSNS.getMessage()
    Logic.handleRequest(message)
  }
}
```

#### Simple lambda function with a custom return type
```scala

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import org.apache.http.HttpStatus

object Logic {
  val log = Logger("logic")
  def handleRequest(event: SNSEvent): String = {
    event.getRecords.get(0).getSNS.getMessage
  }
}

case class Foo(bar: String)

class Function extends LambdaHandler[SNSEvent, Foo] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(snsEvent: SNSEvent, context: Context): Foo = {
    val result = Logic.handleRequest(snsEvent)
    log.info(result)
    Foo(result)
  }
}
```

### SNS Handler
For convenience, there is an `SnsHandler`, this handler automatically decodes the message to the expected `case class`.

```scala
import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.SnsHandler

case class Foo(bar: String)

class Function extends SnsHandler[Foo] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(event: Foo, context: Context): Unit = {
    println(event.bar)
  }
}
```

Notice how in this case the expected type is Foo, the handler will automatically unpack the SNS event and give  your
`execute` method the expected type. 

> **NOTE:**
>
> Unlike the base `LambdaHandler` which is designed to deal with the amazon Java POJOs, this class is designed to work with
> case classes, this way you can define your own events. For convenience an S3 event is provided under :
>
> `io.onema.userverless.events.S3.S3Event`

### API Gateway Handler

#### Handling a valid requests

```scala
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.userverless.function.ApiGatewayHandler
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import org.apache.http.HttpStatus

object Logic {
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    val response = new AwsProxyResponse(HttpStatus.SC_OK)
    response.setBody("{\"message\": \"success\"}")
    response
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val result = Logic.handleRequest(request)
    
    // result.getStatusCode should be (HttpStatus.SC_OK)
    // result.getBody should be ("{\"message\": \"success\"}")
    result
  }
}
```

#### Handling unexpected errors for API Gateway
With the latest version of the framework there is no need to catch any unexpected errors as these will be automatically 
handled as 500 internal server error and a response will be returned with a generic message. 

```scala
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.userverless.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object Logic {
  def handleRequest(request: AwsProxyRequest): Nothing = {
    throw new NotImplementedError("FooBar")
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Logic.handleRequest(request)
    
    // The generated response by the lambda handler will contain the following info
    //result.getBody should be ("{\"message\":\"Internal Server Error: check the logs for more information.\"}")
    //result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
  }
}
```

In this instance the lambda handler will return an `AwsProxyResponse` with a body containing a JSON like this:

```json
{
    "message": "Internal Server Error: check the logs for more information."
}
```

To generate custom messages such as validation errors and present them to the API user, use the provided `HandleRequestException`:

```scala
import io.onema.userverless.exception.HandleRequestException
import org.apache.http.HttpStatus
// ...

object Logic {
  def handleRequest: Nothing = throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "FooBar")
}

class Function extends ApiGatewayHandler {
  // ...
  
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val result = Logic.handleRequest
    
    //result.getBody should be ("{\"message\":\"FooBar\"}")
    //result.getStatusCode should be (HttpStatus.SC_BAD_REQUEST)
    result 
  }
}
```

In this case the message will be 

```json
{
    "message": "FooBar"
}
```
and the response will have the given header `400 BAD REQUEST`.

#### Enable CORS

Cross-Origin Resource Sharing can be enabled in µServerless by passing your function code to the `cors` function. 
The method takes the AwsProxyRequest as an input which is used to get the information about the origin, and it will 
properly add the required headers to the returned `AwsProxyResponse`. To enable Cors, extend the `io.onema.userverless.function.ApiGatewayHandler.Cors`
trait:

```scala
class foo extends ApiGatewayHandler with Cors {
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {

      // Wrapp your code with the followibg block
      cors(request) {
        // Code...
     
        // ... and return an AwsProxyResponse
        new AwsProxyResponse(HttpStatus.SC_OK)
      }
    }
}
```

Your function should extend from the `ApiGatewayHandler` and `ApiGatewayHandler.Cors` and implement the `corsConfiguration` method which should return 
a `CorsConfiguration` object.

```scala 
corsConfiguration(origin: Option[String]): CorsConfiguration = EnvCorsConfiguration(origin)
```

There are four `CorsConfiguration` strategies available:

#### `EnvCorsConfiguration`
Sites are stored in an environment variable called `CORS_SITES` as a comma separated list of origins.
```bash
CORS_SITES=foo.com,bar.com,baz.net
```

#### `DynamodbCorsConfiguration`
You must install the dynamo configuration package to use it:

```
libraryDependencies += "io.onema" %% "userverless-dynamo-config" % "<LATEST_VERSION>"
```

Sites are stored in a DynamoDB table as items in the table. Each site should be Under the `Origin` column.
The following is an example of how to generate the table using a CloudFormation template:

```yaml
Resources:
  CorsDynamoDBTable:
    Type: AWS::DynamoDB::Table
    Properties:
      TableName: !Ref CorsTableName

      AttributeDefinitions:
      - AttributeName: Origin
        AttributeType: S

      KeySchema:
      - AttributeName: Origin
        KeyType: HASH
      ProvisionedThroughput:
        ReadCapacityUnits: 1
        WriteCapacityUnits: 1
```

#### `SsmCorsConfiguration`

SSM Parameter store configuration must be installed using the `userverless-ssm-config` package:
```
libraryDependencies += "io.onema" %% "userverless-ssm-config" % "<LATEST_VERSION>"
```
Sites are stored in a SSM parameter store value called `/cors/sites`. 

> **NOTE**: 
>
> Please note that the stage name will be prepended if one has been set in the `STAGE_NAME` environment variable, 
> reference the [configuration](#lambda-configuration) section for additional information.

#### `NoopCorsConfiguration`
Always returns empty values. This strategy fails validation for all origins and is only 
used as a placeholder by the ApiGateway class.

#### Example
The following is an example of how to return the appropriate `Access-Control-Allow-Origin` header using a custom 
configuration to look up and validate the origin. 

```scala
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import org.apache.http.HttpStatus

import io.onema.userverless.configuration.cors.EnvCorsConfiguration
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.ApiGatewayHandler

class EnvFunction extends ApiGatewayHandler with Cors with NoopLambdaConfiguration {

  //--- Fields ---
  override protected def corsConfiguration(origin: Option[String]) = new EnvCorsConfiguration(origin)

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    cors(request) {
      new AwsProxyResponse(HttpStatus.SC_OK)
    }
    //result.getHeaders.get("Access-Control-Allow-Origin") should be ("https://bar.com")
  }
}
```

## Dealing with exceptions
```scala
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import org.apache.http.HttpStatus

object Logic {
  val log = Logger("logic")
  def handleRequest(event: SNSEvent): Unit = {
    throw new RuntimeException("There was a problem!")
  }
}

class Function extends LambdaHandler[SNSEvent, Unit] with EnvLambdaConfiguration {

  //--- Methods ---
  def execute(snsEvent: SNSEvent, context: Context): Unit = {
    Logic.handleRequest(snsEvent)
  }
}
```

There are a few things to notice here:
 1. The function uses the `EnvLambdaConfiguration` trait. This will enable the handler to get the `SNS_ERROR_TOPIC` 
 environment variable. This is the topic that will be use to report the error. 
     * If we used the `SsmLambdaConfiguration` the name of the error topic is in the parameter `/sns/error/topic`.
     * If we used the `NoopLambdaConfig` errors will never get reported.
 1. The lambda handler rethrows the error after it has been reported.
 
 
### Enable error notifications
If you want to notify a subsystem when lambda functions are failing, you have two options
1. Launch the Overwatch app before launching any of your lambda functions. The Overwatch will report errors of all your µServerless functions to a custom SNS Topic
2. Register a function using the `exceptionListener` method. In this function you can add custom logic to report the exception in any way you want

```scala
  exceptionListener(ex: Throwable => {
    // Exception reporter is a custom 
    snsClient.publish("myCustomTopic", ex.getMessage)
  })
```

##Logging
By default µServerless uses logback to configure logger behavior. as of version `0.0.2` it structures log messages as JSON
using the `logstash-logback-encoder`. In addition to the default logged values 

An log.info like `log.info("This is my message")` Will result in the following log message:
```
*** INFO: {
  "@timestamp": "2018-11-11T16:52:47.502-08:00",
  "@version": "1",
  "message": "This is my message",
  "logger_name": "com.example.App.Function",
  "thread_name": "ScalaTest-run-running-LambdaHandler",
  "level": "INFO",
  "level_value": 20000,
  // Custom values added to each message by the default configuration
  "type": "log",
  "stage": "test",
  "function": "lambda-function-name",
  "lambda_version": "$LATEST"
}
```

### Adding custom properties to the log message 
You may want to attach additional information to your log message in oder to be able to find it better if you forward these emails to a custom db like elastic search. 
To add custom properties to include the use the `keyValue` from the logstash StructuredArguments:
```scala
import net.logstash.logback.argument.StructuredArguments._
...

log.info("This is my message", keyValue("Foo", "bar"), keyValue("BAZ", "BLAH"))
```
This will result in the following message:
```
*** INFO: {
  "@timestamp": "2018-11-11T16:52:47.502-08:00",
  "@version": "1",
  "message": "This is my message",
  "logger_name": "com.example.App.Function",
  "thread_name": "ScalaTest-run-running-LambdaHandler",
  "level": "INFO",
  "level_value": 20000,
  
  // Values added to each message by the default configuration
  "type": "log",
  "stage": "test",
  "function": "lambda-function-name",
  "lambda_version": "$LATEST",
  
  // Custom values
  "Foo": "bar",
  "BAZ": "BLAH"
}
```

### Error logging:
Error logs are handled by a custom log appender. This is to enable µServerless to build a custom error payload when 
exception occur that containing the stack trace.

```
*** ERROR : {
  "message": "Origin 'http://baz.com' is not authorized",
  "exceptionClass": "io.onema.userverless.exception.HandleRequestException",
  "stackTrace": [
    {
      "fileName": "ApiGatewayHandler.scala",
      "lineNumber": 95,
      "className": "io.onema.userverless.function.ApiGatewayHandler$Cors",
      "methodName": "cors()"
    },
    {
      "fileName": "ApiGatewayHandler.scala",
      "lineNumber": 83,
      "className": "io.onema.userverless.function.ApiGatewayHandler$Cors",
      "methodName": "cors$()"
    },
    ...
  ],
  "@timestamp": "2018-11-11T16:52:47.781-0800",
  "type": "exception",
  "stage": "test",
  "function": "lambda-function-name"",
  "lambdaVersion": "$LATEST""
}
```
If you use the error method directly, you will only see a message with the following format:
```
*** ERROR: %msg%n
```
### Metrics
Metrics is a way to log a Statsd compatible message. There are two types of metrics available: `timer` and `count`.

Timers are available as a code-block (thunk):
```scala
import io.onema.userverless.monitoring.LogMetrics.time

time("NameOfMyEvent") {
  val thisIsMyCode = "Foo"
}
```
This will result in the following message:
```
*** METRIC: {
  "@timestamp": "2018-11-11T16:52:47.748-08:00",
  "@version": "1",
  "message": "NameOfMyEvent:0|ms|@1|#stage:test",
  "logger_name": "io.onema.userverless.monitoring.Metrics",
  "thread_name": "ScalaTest-run-running-ApiGatewayHandlerWithCorsTest",
  "level": "INFO",
  "level_value": 20000,
  "metric_name": "NameOfMyEvent",
  "metric_type": "time",
  "type": "metric",
  "stage": "test",
  "function": "lambda-function-name"",
  "lambda_version": "$LATEST""
}
```

A count can be added like such:
```scala
import io.onema.userverless.monitoring.LogMetrics.count

count("NameOfCountEvent")
```
This will result in a log containing the following message:
`NameOfMyEvent:1|c|@1|#stage:test"`

> *NOTE:*
> 
> The metrics are only logged and are not submitted to CloudWatch. To get the metrics reported to CloudWatch you need 
> to use the Overwatch app 

#### Adding custom tags to your metrics
To submit custom tags in your metrics, pass key value pairs to the functions:

```scala
count("CounterName", ("MyKey", "MyValue"), ("AnotherKey", "AnotherValue"))
time("TimerName", ("MyKey", "MyValue"), ("AnotherKey", "AnotherValue"))
```

### Selecting the log level
By default the log level is set to DEBUG, you can overwrite this by setting the `LOG_LEVEL` **environment variable** to one of the log levels:
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`

### Override log configuration
Simply add a `src/main/resources/logback.xml` with your custom configuration and add a custom merging strategy to your `build.sbt`:
```scala
assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
```

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

## Event listeners
There are two events that are triggered during the execution of the function:
1. A validation listener
1. A response listener

### Validation listener
Using the `validationListener` method, you can add functions that take the event as a parameter
and return nothing `TEvent => Unit`. These functions can be used to perform validations before the 
function is called.

### Response listener 
Using the `responseListener` method, you can add functions that take a response as a parameter 
and return a response object `TResponse => TResponse` . These functions can be used to modify the response before 
it is written to the `OutputStream`.

### Exception listener
Using the `exceptionListener` method, you can add functions that take a `Throwable` as a parameter
and returns nothing. These functions can be used to report on exceptions, metrics, and more. 