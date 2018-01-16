/**
  * This file is part of the ONEMA onema Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */
import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import functions.success.Function
import onema.core.json.Implicits.JsonStringToCaseClass
import onema.serverlessbase.function.ApiGatewayResponse
import onema.serverlessbase.model.ErrorMessage
import onema.core.json.Implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}


class ApiGatewayHandlerTest extends FlatSpec with Matchers with MockFactory {
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
    import com.fasterxml.jackson.databind.ObjectMapper
    val mapper = new ObjectMapper
    class Foo extends ApiGatewayResponse {
      def test(): AwsProxyResponse = {
        buildError(200, "test")
      }
    }

    // Act
    val foo = new Foo().test()
    val response = foo.javaClassToJson

    // Assert
    response should be ("{\"statusCode\":200,\"headers\":{\"Access-Control-Allow-Origin\":\"*\"},\"body\":\"{\\\"message\\\":\\\"test\\\"}\",\"base64Encoded\":false}")
  }
}
