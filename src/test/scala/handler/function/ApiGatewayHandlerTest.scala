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
package handler.function

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.sns.AmazonSNSAsync
import com.fasterxml.jackson.databind.ObjectMapper
import functions.success.Function
import handler.EnvironmentHelper
import onema.core.json.Implicits._
import onema.serverlessbase.configuration.lambda.EnvLambdaConfiguration
import onema.serverlessbase.exception.{HandleRequestException, RuntimeException}
import onema.serverlessbase.function.Extensions.RichRegex
import onema.serverlessbase.function.{ApiGatewayHandler, ApiGatewayResponse}
import onema.serverlessbase.model.ErrorMessage
import org.apache.http.HttpStatus
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import ApiGatewayHandlerTest.TestFunction

object ApiGatewayHandlerTest {
  class TestFunction(val snsClient: AmazonSNSAsync) extends ApiGatewayHandler with EnvLambdaConfiguration{

    //--- Methods ---
    def invokeGetRequest(inputStream: InputStream): AwsProxyRequest = {
      getRequest(inputStream)
    }

    def invokeWriteResponse(outputStream: OutputStream, value: AnyRef): Unit = {
      writeResponse(outputStream, value)
    }

    def throwException(): AwsProxyResponse = {
      handle {
        throw new Exception("Test exception")
      }
    }

    def throwHandleRequestException(): AwsProxyResponse = {
      handle {
        throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "Bad request exception")
      }
    }

    def throwRuntimeException(): AwsProxyResponse = {
      handle {
        throw new RuntimeException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Runtime exception")
      }
    }
  }
}

class ApiGatewayHandlerTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {

  "A concrete implementation" should "not throw any exceptions" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new Function

    // Act
    val response = lambdaFunction.lambdaHandler(request, context)
    val body = response.getBody.jsonParse[ErrorMessage]

    // Assert
    body.message should be ("success")
  }

  "An Exception" should "generate a valid response" in {

    // Arrange
    val message = "{\"message\": \"foo bar\"}"

    // Act
    val errorMessage = message.jsonParse[ErrorMessage]

    // Assert
    errorMessage.message should be ("foo bar")
  }

  "A response" should "be properly serialized to json" in {

    // Arrange
    val mapper = new ObjectMapper
    class Foo extends ApiGatewayResponse {
      def test(): AwsProxyResponse = {
        buildError(HttpStatus.SC_OK, "test")
      }
    }
    val expectedValue = "{\"statusCode\":200,\"headers\":null,\"body\":\"{\\\"message\\\":\\\"test\\\"}\",\"base64Encoded\":false}"

    // Act
    val foo = new Foo().test()
    val response = foo.javaClassToJson

    // Assert
    response should be (expectedValue)
  }

  "An input stream" should "generate the proper request" in {

    // Arrange
    val message = "{\n  \"body\": \"{\\\"test\\\":\\\"body\\\"}\",\n  \"resource\": \"/{proxy+}\",\n  \"requestContext\": {\n    \"resourceId\": \"123456\",\n    \"apiId\": \"1234567890\",\n    \"resourcePath\": \"/{proxy+}\",\n    \"httpMethod\": \"POST\",\n    \"requestId\": \"c6af9ac6-7b61-11e6-9a41-93e8deadbeef\",\n    \"accountId\": \"123456789012\",\n    \"identity\": {\n      \"apiKey\": null,\n      \"userArn\": null,\n      \"cognitoAuthenticationType\": null,\n      \"caller\": null,\n      \"userAgent\": \"Custom User Agent String\",\n      \"user\": null,\n      \"cognitoIdentityPoolId\": null,\n      \"cognitoIdentityId\": null,\n      \"cognitoAuthenticationProvider\": null,\n      \"sourceIp\": \"127.0.0.1\",\n      \"accountId\": null\n    },\n    \"stage\": \"prod\"\n  },\n  \"queryStringParameters\": {\n    \"foo\": \"bar\"\n  },\n  \"headers\": {\n    \"Via\": \"1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)\",\n    \"Accept-Language\": \"en-US,en;q=0.8\",\n    \"CloudFront-Is-Desktop-Viewer\": \"true\",\n    \"CloudFront-Is-SmartTV-Viewer\": \"false\",\n    \"CloudFront-Is-Mobile-Viewer\": \"false\",\n    \"X-Forwarded-For\": \"127.0.0.1, 127.0.0.2\",\n    \"CloudFront-Viewer-Country\": \"US\",\n    \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\",\n    \"Upgrade-Insecure-Requests\": \"1\",\n    \"X-Forwarded-Port\": \"443\",\n    \"Host\": \"1234567890.execute-api.us-east-1.amazonaws.com\",\n    \"X-Forwarded-Proto\": \"https\",\n    \"X-Amz-Cf-Id\": \"cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==\",\n    \"CloudFront-Is-Tablet-Viewer\": \"false\",\n    \"Cache-Control\": \"max-age=0\",\n    \"User-Agent\": \"Custom User Agent String\",\n    \"CloudFront-Forwarded-Proto\": \"https\",\n    \"Accept-Encoding\": \"gzip, deflate, sdch\"\n  },\n  \"pathParameters\": {\n    \"proxy\": \"path/to/resource\"\n  },\n  \"httpMethod\": \"POST\",\n  \"stageVariables\": {\n    \"baz\": \"qux\"\n  },\n  \"path\": \"/path/to/resource\"\n}"
    val inputStream = new ByteArrayInputStream(message.getBytes)
    val snsMock = mock[AmazonSNSAsync]
    val function = new TestFunction(snsMock)

    // Act
    val result = function.invokeGetRequest(inputStream)

    // Assert
    result.getBody should be ("{\"test\":\"body\"}")
    result.getHttpMethod should be ("POST")
  }

  "An write to an output stream" should "should correctly serialize and write json" in {

    // Arrange
    case class TestClass(foo: String, bar: String, baz: Int)
    val outputStream = new ByteArrayOutputStream()
    val snsMock = mock[AmazonSNSAsync]
    val function = new TestFunction(snsMock)
    val response = new AwsProxyResponse()

    // Act
    function.invokeWriteResponse(outputStream, response)
    val result = new String(outputStream.toByteArray, java.nio.charset.StandardCharsets.US_ASCII)

    // Assert
    result should be ("{\"statusCode\":0,\"headers\":null,\"body\":null,\"base64Encoded\":false}")
  }

  "An exception" should "generate the proper response" in {

    // Arrange
    val snsMock = mock[AmazonSNSAsync]
    val function = new TestFunction(snsMock)

    // Act
    val result = function.throwException()

    // Assert
    result.getBody should be ("{\"message\":\"Internal Server Error: check the logs for more information.\"}")
    result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
  }

  "An exception" should "generate the proper response and send notification to SNS if topic is set" in {

    // Arrange
    setEnv("SNS_ERROR_TOPIC", "fooBar")

    val snsMock = mock[AmazonSNSAsync]
    (snsMock.publish(_: String, _: String)).expects("fooBar", *)
    val function = new TestFunction(snsMock)

    // Act
    val result = function.throwException()

    // Assert
    result.getBody should be ("{\"message\":\"Internal Server Error: check the logs for more information.\"}")
    result.getStatusCode should be (HttpStatus.SC_INTERNAL_SERVER_ERROR)
  }

  "A handle request exception" should "should generate the proper response" in {

    // Arrange
    val snsMock = mock[AmazonSNSAsync]
    val function = new TestFunction(snsMock)

    // Act
    val result = function.throwHandleRequestException()

    // Assert
    result.getBody should be ("{\"message\":\"Bad request exception\"}")
    result.getStatusCode should be (HttpStatus.SC_BAD_REQUEST)
  }

  "A handle runtime exception" should "should generate the proper response" in {

    // Arrange
    val snsMock = mock[AmazonSNSAsync]
    val function = new TestFunction(snsMock)

    // Act
    val result = function.throwRuntimeException()

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
