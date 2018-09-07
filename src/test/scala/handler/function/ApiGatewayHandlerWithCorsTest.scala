/**
  * This file is part of the ONEMA io.onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */
package handler.function

import java.io.ByteArrayOutputStream

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, DescribeTableResult, GetItemRequest, GetItemResult}
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync
import com.amazonaws.services.simplesystemsmanagement.model.{GetParameterRequest, GetParameterResult, Parameter}
import functions.cors.{DynamodbFunction, EnvFunction, NoopFunction, SsmFunction}
import handler.EnvironmentHelper
import io.onema.userverless.configuration.cors.DynamodbCorsConfiguration
import io.onema.userverless.configuration.cors.Extensions._
import handler.function.ApiGatewayTestHelper._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class ApiGatewayHandlerWithCorsTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {

  "A function with CORS enabled using env vars" should "return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "https://foo.com"
    setEnv("CORS_SITES", originSite)
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)
    val headers = Option(response.getHeaders).getOrElse(Map().asJava)

    // Assert
    headers.get("Access-Control-Allow-Origin") should be (originSite)
    headers.get("Access-Control-Allow-Credentials") should  be ("true")
  }

  "A function with CORS enabled using env vars with multiple values" should "return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "https://foo.com,https://bar.com,http://baz.com"
    val site = "http://baz.com"
    setEnv("CORS_SITES", originSite)
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> site).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be (site)
  }

  "A function with CORS enabled using empty env vars " should "NOT return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = ""
    val site = "http://baz.com"
    setEnv("CORS_SITES", originSite)
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> site).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    Option(response.getHeaders) should be (None)
  }

  "A function with CORS enabled using empty env vars " should "NOT return response with access-control-allow-origin header if site is not set" in {
    // Arrange
    val originSite = ""
    setEnv("CORS_SITES", originSite)
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    Option(response.getHeaders) should be (None)
  }

  "A function with CORS enabled using empty env vars " should "NOT return response with access-control-allow-origin header if site is an empty string" in {
    // Arrange
    val originSite = ""
    val site = ""
    setEnv("CORS_SITES", originSite)
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    //    request.setHeaders(Map("origin" -> site).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    Option(response.getHeaders) should be (None)
  }

  "A function with CORS enabled using env vars and CORS_SITE set to '*' " should "return response with access-control-allow-origin:* header" in {
    // Arrange
    val originSite = "bar.com"
    setEnv("CORS_SITES", "*")
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be ("bar.com")
  }

  "A function with CORS enabled using DynamoDB" should "return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "bar.com"
    val tableName = "Origins"
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    val getItemResult = new GetItemResult().withItem(Map("Origin" -> new AttributeValue(originSite)).asJava)
    val tableResults = new DescribeTableResult()
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).returning(getItemResult)
    (clientMock.describeTable(_: String)).expects(tableName).returning(tableResults)
    request.setHeaders(Map("origin" -> originSite).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val corsConfiguration = new DynamodbCorsConfiguration(Some("bar.com"), tableName, clientMock)

    // Act
    val isEnabled = corsConfiguration.isEnabled
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output).withCors(corsConfiguration)

    // Assert
    isEnabled should be (true)
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be ("bar.com")
  }

  "A function with CORS enabled using DynamoDB" should "not return response with access-control-allow-origin header if origin is *" in {
    // Arrange
    val originSite = "*"
    val request = new AwsProxyRequest()
    val getItemResult = new GetItemResult()
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).returning(getItemResult)
    val lambdaFunction = new DynamodbFunction("foo", clientMock)
    request.setHeaders(Map("origin" -> originSite).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    Option(response.getHeaders).isDefined should be (false)
  }

  "A function with CORS enabled using DynamoDB" should "not return response with access-control-allow-origin header if origin is None" in {
    // Arrange
    val originSite: String = null
    val dynamoConfig = DynamodbCorsConfiguration(originSite)

    // Act
    val response = dynamoConfig.isOriginValid

    // Assert
    response should be (false)
  }

  "Create a simple Cors Config using DynamoDB and origin" should "return an object with the valid origin" in {
    // Arrange
    val originSite = "https://foo.com"
    val corsConfig = DynamodbCorsConfiguration(originSite)

    // Act
    val originOption = corsConfig.origin

    // Assert
    originOption.isDefined should be (true)
    originOption.getOrElse("") should be (originSite)
  }

  "A function with Noop CORS configuration" should " not return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "https://baz.com"
    val lambdaFunction = new NoopFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val output = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    Option(response.getHeaders) should be (None)
  }

  "A function with CORS enabled using ssm parameter store" should "return response with access-control-allow-origin header" in {

    // Arrange
    deleteEnv("STAGE_NAME")
    val originSite = "https://foo.com"
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext
    val paramRequest = new GetParameterRequest().withName("/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/cors/sites").withValue(originSite))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(paramRequest).returning(result).repeat(2)
    val output = new ByteArrayOutputStream()
    val lambdaFunction = new SsmFunction(ssmClientMock)

    // Act
    lambdaFunction.lambdaHandler(toInputStream(request), output, context)
    val response = outputToResponse(output)

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be (originSite)
  }
}
