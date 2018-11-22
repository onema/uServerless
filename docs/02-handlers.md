# Handlers available
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
