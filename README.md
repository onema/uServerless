Serverless Base for Scala
=========================
![Build Status](https://codebuild.us-east-1.amazon.com/badges?uuid=95007942-9a19-4beb-917b-4d35775f97b8&branch=master)

The serverless base package is a small collection of base classes to
build AWS Lambda functions using scala. The base classes have the following
properties:

1. Defines a method to create lambda functions by separating the `Function`
entrypoint from the `Logic` of the function.
  - The `Function` is responsible for building all the dependencies and constructing the logic object
  - The `Logic` should aquire all it's dependencies via DI, hence it should be the testable part of your code
1. Handle errors and submit notifications via SNS with error description
1. API Gateway has a special handler that handles with Lambda Proxy Request and Responses
1. Provides a trait to get configuration values or secrets from SSM Parameter Store 

Invocation model
----------------
ServerlessBase is designed to separate the lambda entry point from the business logic. 
The lambda entry point is a class we call `Function`, and it is responsible for constructing all the dependencies and injecting them 
to a class or object we call `Logic`. The function is also responsible for invoking the `Logic`. To do so we pass the methods we
want to call to a `handler` method. The `handler` invokes the `Logic` method, and caches any errors logging them and reporting these via an SNS topic if this option is enabled.

### Handlers available
There are two handler traits available at this time:
* LambdaHandler: A generic handler that does not return anything. 
* ApiGatewayHandler: An APIGateway specific handler that generates an API Proxy request. It has support methods to deal with java input and output streams; these are used when using apis that have a cognito custom authorizer.

API Gateway Handler Usage
--------------------------
### Handling valid requests
```scala

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import onema.serverlessbase.function.ApiGatewayHandler
import onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import org.apache.http.HttpStatus

object Logic {
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    val response = new AwsProxyResponse(HttpStatus.SC_OK)
    response.setBody("{\"message\": \"success\"}")
    response
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val result = handle(() => Logic.handleRequest(request))
    result.getStatusCode should be (HttpStatus.SC_OK)
    result.getBody should be ("{\"message\": \"success\"}")
    result
  }
}
```

### Handling unexpected errors for API Gateway
```scala
import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import onema.serverlessbase.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object Logic {
  def handleRequest(request: AwsProxyRequest): Nothing = {
    throw new NotImplementedError("FooBar")
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val result = handle(() => Logic.handleRequest(request))
    result.getBody should be ("{\"message\":\"Internal Server Error: check the logs for more information.\"}")
    result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
    result
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

import onema.serverlessbase.exception.HandleRequestException
import org.apache.http.HttpStatus
// ...

object Logic {
  def handleRequest: Nothing = throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "FooBar")
}

class Function extends ApiGatewayHandler {
  // ...
  
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val result = handle(() => Logic.handleRequest)
    result.getBody should be ("{\"message\":\"FooBar\"}")
    result.getStatusCode should be (HttpStatus.SC_BAD_REQUEST)
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

Lambda Configuration
--------------------

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
The environment name is set as the environment variable `ENVIRONMENT_NAME`.

To use the SSM environment variable in  your function simply extend from the `SsmLambdaConfiguration` trait like such

```scala
class TestFunction(snsClientMock: AmazonSNSAsync) extends LambdaHandler with SsmLambdaConfiguration {
  val foo: Option[String] = getValue("/foo")
  val barHierarchy: Map[String, String] = getValues("/bar/")
}
```

Enable CORS
-----------
The following is an example of how to return the appropriate `Access-Control-Allow-Origin` header using a custom 
strategy to look up and validate the origin. 

Simply import the `AwsProxyResponseExtension` and call the `withCors(Strategy)` method on it.

There are four strategies implemented:
* `DynamodbCorsConfiguration`: Sites are stored in a DynamoDB table as items in the table. Each site should be Under the `Origin` column
* `EnvCorsConfiguration`: Sites are stored in an environment variable called `CORS_SITES` as a comma separated list of origins
* `NoopCorsConfiguration`: Always returns empty values
* `SsmCorsConfiguration`: Sites are stored in a SSM parameter store value called `/cors/sites`. Please note that the stage name will be prepended if one has been set in the `STAGE_NAME` environment variable

```scala
import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import onema.serverlessbase.configuration.cors.EnvCorsConfiguration
import onema.serverlessbase.configuration.cors.Extensions.AwsProxyResponseExtension
import onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import onema.serverlessbase.function.ApiGatewayHandler
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
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val origin = request.getHeaders.get("origin")
    val result = handle(() => EnvLogic.handleRequest(request)).withCors(new EnvCorsConfiguration(origin))
    result.getHeaders.get("Access-Control-Allow-Origin") should be ("https://bar.com")
    result
  }
}
```
Enable error notifications
--------------------------
If you want to notify a subsystem when lambda functions are failing, you can do so by setting the configuration key `/sns/error/topic`. 
This should contain the ARN of a valid SNS error topic that will be used to deliver the error generated by the lambda function.  
