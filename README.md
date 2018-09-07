# µServerless for Scala
[![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiTnE1eURWUEdFUER4Q1JnRnJTcDRBcFdjdkU1d3gwYzhkYTB5MGkvdE5RTnpBNUhueDZhem9sdkFPOUlOZklVVWppampsVHhBSCtGL3FkSE1ld2tkcFFzPSIsIml2UGFyYW1ldGVyU3BlYyI6IllZTVZ5L0FRS0syMkZTRDMiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)](https://console.aws.amazon.com/codebuild/home?region=us-east-1#/projects/uServerless-master/view)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/634b14e124ec44429b1b81fdbcb6548f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/UServerless&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/634b14e124ec44429b1b81fdbcb6548f)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/UServerless&amp;utm_campaign=Badge_Coverage)
[![LICENSE](https://img.shields.io/badge/license-Apache--2.0-blue.svg?longCache=true&style=flat-square)](LICENSE)

The µServerless package (pronounced micro-serverless) is a small collection of classes, traits and adapters to help you
build AWS Lambda functions using scala. 

## Install
Add the following to your SBT config:

```
libraryDependencies += "io.onema" %% "userverless" % "0.1.0"
```

## Invocation model
The adapters have the following properties:

1. Enable you to separate the lambda `Function` handler from the `Logic` of the application.
  - The `Function` is responsible for building all the dependencies and constructing the logic object and handling errors, notifications and keeping the function warm (if enabled).
  - The `Logic` should acquire all it's dependencies via DI, hence it should be the testable part of your code.
1. Handle and report errors as notifications via SNS
1. API Gateway has a special handler that to help you deal with Lambda Proxy Request and Responses
1. Provides a trait to get configuration values or secrets from SSM Parameter Store 

### Handlers available
There are two traits that can be used in your functions:
* LambdaHandler: A generic handler that uses a type parameters to defined the handler event and return type. 
* ApiGatewayHandler: An APIGateway specific handler that generates an API Proxy request. 
It has support methods to deal with java input and output streams; these are used when using apis that have a cognito custom authorizer.

## Lambda Handler Usage

### Simple lambda function with Unit return type
```scala

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.serverlessbase.function.LambdaHandler
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
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

### Simple lambda function with a custom return type
```scala

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.serverlessbase.function.LambdaHandler
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import org.apache.http.HttpStatus

object Logic {
  val log = Logger("logic")
  def handleRequest(event: SNSEvent): String = {
    event.getRecords.get(0).getSNS.getMessage
  }
}

class Function extends LambdaHandler[SNSEvent, String] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(snsEvent: SNSEvent, context: Context): String = {
    val result = Logic.handleRequest(snsEvent)
    log.info(result)
    result
  }
}
```

### Dealing with exceptions
```scala

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.serverlessbase.function.LambdaHandler
import io.onema.serverlessbase.configuration.lambda.EnvLambdaConfiguration
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


## API Gateway Handler Usage

### Handling a valid requests
```scala

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.serverlessbase.function.ApiGatewayHandler
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
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
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val result = Logic.handleRequest(request)
    
    // result.getStatusCode should be (HttpStatus.SC_OK)
    // result.getBody should be ("{\"message\": \"success\"}")
    result
  }
}
```

### Handling unexpected errors for API Gateway
With the latest version of the framework there is no need to catch any unexpected errors as these will be automatically 
handled as 500 internal server error and a response will be returned with a generic message. 
If you wish to 
```scala
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.serverlessbase.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object Logic {
  def handleRequest(request: AwsProxyRequest): Nothing = {
    throw new NotImplementedError("FooBar")
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Methods ---
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
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

import io.onema.serverlessbase.exception.HandleRequestException
import org.apache.http.HttpStatus
// ...

object Logic {
  def handleRequest: Nothing = throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "FooBar")
}

class Function extends ApiGatewayHandler {
  // ...
  
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
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

## Lambda Configuration

ServerlessBase supports three methods to get configuration values: environment variables, SSM Parameter Store, and Noop.

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

To use the SSM environment variable in  your function simply extend from the `SsmLambdaConfiguration` trait like such

```scala
class TestFunction(snsClientMock: AmazonSNSAsync) extends LambdaHandler with SsmLambdaConfiguration {
  val foo: Option[String] = getValue("/foo")
  val barHierarchy: Map[String, String] = getValues("/bar/")
}
```

## Enable CORS

The following is an example of how to return the appropriate `Access-Control-Allow-Origin` header using a custom 
strategy to look up and validate the origin. 

Simply import the `AwsProxyResponseExtension` and call the `withCors(Strategy)` method on it.

There are four strategies implemented:
* `DynamodbCorsConfiguration`: Sites are stored in a DynamoDB table as items in the table. Each site should be Under the `Origin` column
* `EnvCorsConfiguration`: Sites are stored in an environment variable called `CORS_SITES` as a comma separated list of origins
* `NoopCorsConfiguration`: Always returns empty values
* `SsmCorsConfiguration`: Sites are stored in a SSM parameter store value called `/cors/sites`. Please note that the stage name will be prepended if one has been set in the `STAGE_NAME` environment variable

```scala
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.serverlessbase.configuration.cors.EnvCorsConfiguration
import io.onema.serverlessbase.configuration.cors.Extensions.AwsProxyResponseExtension
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import io.onema.serverlessbase.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object EnvLogic {
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    new AwsProxyResponse(HttpStatus.SC_OK)
  }
}

class EnvFunction extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val origin = request.getHeaders.get("origin")
    val result = EnvLogic.handleRequest(request)
                   .withCors(new EnvCorsConfiguration(origin))
    //result.getHeaders.get("Access-Control-Allow-Origin") should be ("https://bar.com")
    result
  }
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


## Enable error notifications

If you want to notify a subsystem when lambda functions are failing, you can do so by setting the configuration key `/sns/error/topic`. 
This should contain the ARN of a valid SNS error topic that will be used to deliver the error generated by the lambda function.  
