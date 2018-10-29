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

import java.io._

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSAsync
import functions.success.Function
import handler.EnvironmentHelper
import handler.function.ApiGatewayHandlerTest._
import io.onema.userverless.configuration.lambda.{EnvLambdaConfiguration, MemoryLambdaConfiguration}
import io.onema.userverless.exception.{HandleRequestException, RuntimeException}
import io.onema.userverless.function.Extensions._
import io.onema.userverless.function.{ApiGatewayHandler, ApiGatewayResponse}
import io.onema.userverless.model.ErrorMessage
import io.onema.userverless.test.TestJavaObjectExtensions._
import org.apache.http.HttpStatus
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

object ApiGatewayHandlerTest {

  case class Body(response: String)

  class TestApiGateway500ErrorFunction(sns: AmazonSNSAsync) extends ApiGatewayHandler with MemoryLambdaConfiguration {

    //--- Fields ---
    override protected lazy val snsClient: AmazonSNSAsync = sns

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      throw new Exception("Test exception")
    }

    override protected def map: Map[String, String] = Map("/sns/error/topic" -> "foo")
  }

  class TestApiGateway400ErrorFunction(sns: AmazonSNSAsync) extends ApiGatewayHandler with MemoryLambdaConfiguration {

    //--- Fields ---
    override protected lazy val snsClient: AmazonSNSAsync = sns

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "Bad request exception")
    }

    override protected def map: Map[String, String] = Map("/sns/error/topic" -> "foo")
  }

  class TestApiGatewayRuntimeErrorFunction(sns: AmazonSNSAsync) extends ApiGatewayHandler with MemoryLambdaConfiguration {

    //--- Fields ---
    override protected lazy val snsClient: AmazonSNSAsync = sns

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      throw new RuntimeException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Runtime exception")
    }

    override protected def map: Map[String, String] = Map("/sns/error/topic" -> "foo")
  }

  class TestApiGatewayEchoFunction(sns: AmazonSNSAsync) extends ApiGatewayHandler with EnvLambdaConfiguration {

    //--- Fields ---
    override protected lazy val snsClient: AmazonSNSAsync = sns

    //--- Methods ---
    def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
      val response = new AwsProxyResponse()
      response.setBody(request.getBody)
      response
    }
  }

  class ValidResponseFunction extends ApiGatewayResponse {
    def test(): AwsProxyResponse = {
      buildResponse(HttpStatus.SC_OK, payload = Body("test"), Map("foo" -> "bar"))
    }
  }

  class ValidResponseHeadersOnlyFunction extends ApiGatewayResponse {
    def test(): AwsProxyResponse = {
      buildResponse(HttpStatus.SC_OK, Map("foo" -> "bar"))
    }
  }

  class ErrorResponseFunction extends ApiGatewayResponse {
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

  "An Exception" should "generate a valid response" in {

    // Arrange
    import io.onema.json.Extensions._
    val message = "{\"message\": \"foo bar\"}"

    // Act
    val errorMessage = message.jsonDecode[ErrorMessage]

    // Assert
    errorMessage.message should be ("foo bar")
  }

  "An error response" should "be properly serialized to json" in {

    // Arrange
    import io.onema.json.JavaExtensions._
    val expectedValue = "{\"statusCode\":507,\"headers\":null,\"body\":\"{\\\"message\\\":\\\"test\\\"}\",\"base64Encoded\":false}"

    // Act
    val foo = new ErrorResponseFunction().test()
    val response = foo.asJson

    // Assert
    response should be (expectedValue)
  }

  "A response with custom headers and str" should "be properly serialized to json" in {

    // Arrange
    import io.onema.json.JavaExtensions._
    val expectedValue = "{\"statusCode\":200,\"headers\":{\"foo\":\"bar\"},\"body\":\"{\\\"response\\\":\\\"test\\\"}\",\"base64Encoded\":false}"

    // Act
    val foo = new ValidResponseFunction().test()
    val response = foo.asJson

    // Assert
    response should be(expectedValue)
  }

  "A response with custom headers" should "be properly serialized to json" in {

    // Arrange
    import io.onema.json.JavaExtensions._
    val expectedValue = "{\"statusCode\":200,\"headers\":{\"foo\":\"bar\"},\"body\":null,\"base64Encoded\":false}"

    // Act
    val foo = new ValidResponseHeadersOnlyFunction().test()
    val response = foo.asJson

    // Assert
    response should be(expectedValue)
  }

  "An exception" should "generate the proper response" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val snsMock = mock[AmazonSNSAsync]
    val function = new TestApiGateway500ErrorFunction(snsMock)

    // Act
    function.lambdaHandler(request.toInputStream, output, context)
    val result = output.toObject[AwsProxyResponse]

    // Assert
    result.getBody should be ("{\"message\":\"Internal Server Error: check the logs for more information.\"}")
    result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
  }

  "An exception" should "generate the proper response and send notification to SNS if topic is set" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val snsMock = mock[AmazonSNSAsync]
      (snsMock.publish(_: String, _: String)).expects(*, *)
    val function = new TestApiGateway500ErrorFunction(snsMock)

    // Act - Assert
    function.lambdaHandler(request.toInputStream, output, context)
  }

  "A handle request exception" should "should generate the proper response" in {

    // Arrange
    val snsMock = mock[AmazonSNSAsync]
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val function = new TestApiGateway400ErrorFunction(snsMock)

    // Act
    function.lambdaHandler(request.toInputStream, output, context)
    val result = output.toObject[AwsProxyResponse]

    // Assert
    result.getBody should be ("{\"message\":\"Bad request exception\"}")
    result.getStatusCode should be (HttpStatus.SC_BAD_REQUEST)
  }

  "A handle runtime exception" should "should generate the proper response" in {

    // Arrange
    val snsMock = mock[AmazonSNSAsync]
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val output = new ByteArrayOutputStream()
    val function = new TestApiGatewayRuntimeErrorFunction(snsMock)

    // Act
    function.lambdaHandler(request.toInputStream, output, context)
    val result = output.toObject[AwsProxyResponse]

    // Assert
    result.getBody should be ("{\"message\":\"Runtime exception\"}")
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