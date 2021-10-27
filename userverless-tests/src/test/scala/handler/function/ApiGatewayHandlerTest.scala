/*
 * This file is part of the ONEMA userverless-tests Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2018-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */
package handler.function

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import functions.success.{Function, FunctionIO}
import handler.EnvironmentHelper
import handler.function.ApiGatewayHandlerTest._
import io.onema.userverless.config.{EnvLambdaConfiguration, MemoryLambdaConfiguration}
import io.onema.userverless.domain.ApiGatewayErrorMessage
import io.onema.userverless.exception.{HandleRequestException, RuntimeException}
import io.onema.userverless.service.{ApiGatewayHandler, ApiGatewayResponseBuilder}
import io.onema.userverless.test.TestJavaObjectExtensions._
import org.apache.http.HttpStatus
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import java.io._

object ApiGatewayHandlerTest {

  case class Body(response: String)

  class TestApiGateway500ErrorFunction() extends ApiGatewayHandler with MemoryLambdaConfiguration {

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      throw new Exception("Test exception")
    }

    override protected def map: Map[String, String] = Map("/sns/error/topic" -> "foo")
  }

  class TestApiGateway400ErrorFunction() extends ApiGatewayHandler with MemoryLambdaConfiguration {

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "Bad request exception")
    }

    override protected def map: Map[String, String] = Map("/sns/error/topic" -> "foo")
  }

  class TestApiGatewayRuntimeErrorFunction() extends ApiGatewayHandler with MemoryLambdaConfiguration {

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      throw new RuntimeException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Runtime exception")
    }

    override protected def map: Map[String, String] = Map("/sns/error/topic" -> "foo")
  }

  class TestApiGatewayEchoFunction() extends ApiGatewayHandler with EnvLambdaConfiguration {

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      val response = new AwsProxyResponse()
      response.setBody(request.getBody)
      response
    }
  }

  class ValidResponseFunction extends ApiGatewayResponseBuilder {
    def test(): AwsProxyResponse = {
      buildResponse(HttpStatus.SC_OK, payload = Body("test"), headers = Map("foo" -> "bar"))
    }
  }

  class ValidResponseHeadersOnlyFunction extends ApiGatewayResponseBuilder {
    def test(): AwsProxyResponse = {
      buildResponse(HttpStatus.SC_OK, headers = Map("foo" -> "bar"))
    }
  }

  class ErrorResponseFunction extends ApiGatewayResponseBuilder {
    def test(): AwsProxyResponse = {
      buildError(HttpStatus.SC_INSUFFICIENT_STORAGE, "test")
    }
  }
}

class ApiGatewayHandlerTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {

  "A concrete implementation" should "not throw any exceptions" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new Function
    val output = new ByteArrayOutputStream()

    // Act
    lambdaFunction.lambdaHandler(request.toInputStream, output, context)
    val response: AwsProxyResponse = output.toObject[AwsProxyResponse]
    val body = response.getBody.toErrorMessage

    // Assert
    body.message should be ("success")
  }

  "A concrete implementation that returns an IO" should "not throw any exceptions" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new FunctionIO
    val output = new ByteArrayOutputStream()

    // Act
    lambdaFunction.lambdaHandler(request.toInputStream, output, context)
    val response: AwsProxyResponse = output.toObject[AwsProxyResponse]
    val body = response.getBody.toErrorMessage

    // Assert
     body.message should be ("success")
  }

  "An Exception" should "generate a valid response" in {

    // Arrange
    import io.onema.json.Extensions._
    val message = "{\"message\": \"foo bar\"}"

    // Act
    val errorMessage = message.jsonDecode[ApiGatewayErrorMessage]

    // Assert
    errorMessage.message should be ("foo bar")
  }

  "An error response" should "be properly serialized to json" in {

    // Arrange
    import io.onema.json.JavaExtensions._
    val expectedValue = """{"statusCode":507,"headers":{},"body":"{\"message\":\"test\",\"cause\":\"\"}","isBase64Encoded":false}"""

    // Act
    val response = new ErrorResponseFunction().test().asJson

    // Assert
    response should be (expectedValue)
  }

  "A response with custom headers and str" should "be properly serialized to json" in {

    // Arrange
    import io.onema.json.JavaExtensions._
    val expectedValue = """{"statusCode":200,"headers":{"foo":"bar"},"body":"{\"response\":\"test\"}","isBase64Encoded":false}"""

    // Act
    val response = new ValidResponseFunction().test().asJson

    // Assert
    response should be(expectedValue)
  }

  "A response with custom headers" should "be properly serialized to json" in {

    // Arrange
    import io.onema.json.JavaExtensions._
    val expectedValue = """{"statusCode":200,"headers":{"foo":"bar"},"isBase64Encoded":false}"""

    // Act
    val response = new ValidResponseHeadersOnlyFunction().test().asJson

    // Assert
    response should be(expectedValue)
  }

  "An exception" should "generate the proper response" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val function = new TestApiGateway500ErrorFunction()

    // Act
    function.lambdaHandler(request.toInputStream, output, context)
    val result = output.toObject[AwsProxyResponse]

    // Assert
    result.getBody should be ("""{"message":"Internal Server Error: check the logs for more information.","cause":""}""")
    result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
  }

  "An exception" should "generate the proper response and send notification to SNS if topic is set" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val function = new TestApiGateway500ErrorFunction()

    // Act - Assert
    function.lambdaHandler(request.toInputStream, output, context)
  }

  "A handle request exception" should "should generate the proper response" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val function = new TestApiGateway400ErrorFunction()

    // Act
    function.lambdaHandler(request.toInputStream, output, context)
    val result = output.toObject[AwsProxyResponse]

    // Assert
    result.getBody should be ("""{"message":"Bad request exception","cause":""}""")
    result.getStatusCode should be (HttpStatus.SC_BAD_REQUEST)
  }

  "A handle runtime exception" should "should generate the proper response" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val function = new TestApiGatewayRuntimeErrorFunction()

    // Act
    function.lambdaHandler(request.toInputStream, output, context)
    val result = output.toObject[AwsProxyResponse]

    // Assert
    result.getBody should be ("""{"message":"Runtime exception","cause":""}""")
    result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
  }

  "A regex " should "match true when using a valid expression" in {
    // Arrange
    val regex = "[\\d]{4}".r
    val value = "1234"

    // Act
    val result = regex.matches(value)

    // Assert
    result should be (true)
  }

  "A regex" should "not match when using an invalid expression" in {
    // Arrange
    val regex = "[\\d]{4}".r
    val value = "OneTwoThreeFour"

    // Act
    val result = regex matches value

    // Assert
    result should be (false)
  }
}
