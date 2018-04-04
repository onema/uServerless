/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */
package handler

import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item, Table}
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, GetItemRequest, GetItemResult}
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync
import com.amazonaws.services.simplesystemsmanagement.model.{GetParameterRequest, GetParameterResult, Parameter}
import functions.cors.{EnvFunction, NoopFunction, SsmFunction}
import onema.serverlessbase.configuration.cors.DynamodbCorsConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import onema.serverlessbase.configuration.cors.Extensions._

import scala.collection.JavaConverters._

class ApiGatewayHandlerWithCorsTest extends FlatSpec with BeforeAndAfter with Matchers with MockFactory with EnvironmentHelper {

  before {
    deleteEnv("CORS_SITES")
    deleteEnv("STAGE_NAME")
  }

  "A function with CORS enabled using env vars" should "return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "https://foo.com"
    setEnv("CORS_SITES", originSite)
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext

    // Act
    val response = lambdaFunction.lambdaHandler(request, context)

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be (originSite)
  }

  "A function with CORS enabled using env vars and CORS_SITE set to '*' " should "return response with access-control-allow-origin:* header" in {
    // Arrange
    val originSite = "bar.com"
    setEnv("CORS_SITES", "*")
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext

    // Act
    val response = lambdaFunction.lambdaHandler(request, context)

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be ("bar.com")
  }

  "A function with CORS enabled using DynamoDB" should "return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "bar.com"
    val lambdaFunction = new EnvFunction()
    val request = new AwsProxyRequest()
    val getItemResult = new GetItemResult().withItem(Map("Origin" -> new AttributeValue(originSite)).asJava)
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).returning(getItemResult)
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext

    // Act
    val response = lambdaFunction.lambdaHandler(request, context).withCors(new DynamodbCorsConfiguration(Some("bar.com"), "Origin", clientMock))

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be ("bar.com")
  }

  "A function with Noop CORS configuration" should " not return response with access-control-allow-origin header" in {
    // Arrange
    val originSite = "https://baz.com"
    val lambdaFunction = new NoopFunction()
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext

    // Act
    val response = lambdaFunction.lambdaHandler(request, context)

    // Assert
    response.getHeaders should be (null)
  }

  "A function with CORS enabled using ssm parameter store" should "return response with access-control-allow-origin header" in {

    // Arrange
    val originSite = "https://foo.com"
    val request = new AwsProxyRequest()
    request.setHeaders(Map("origin" -> originSite).asJava)
    val context = new MockLambdaContext
    val paramRequest = new GetParameterRequest().withName("/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/cors/sites").withValue(originSite))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(paramRequest).returning(result).repeat(2)
    val lambdaFunction = new SsmFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.lambdaHandler(request, context)

    // Assert
    response.getHeaders.containsKey("Access-Control-Allow-Origin") should be (true)
    response.getHeaders.get("Access-Control-Allow-Origin") should be (originSite)
  }
}
