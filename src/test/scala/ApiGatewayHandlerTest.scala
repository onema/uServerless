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
import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import onema.function.SuccessFunction
import onema.serverlessbase.core.json.Implicits.JsonStringToCaseClass
import onema.serverlessbase.model.ErrorMessage
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}


class ApiGatewayHandlerTest extends FlatSpec with Matchers with MockFactory {
  "A concrete implementeation of a generic class" should "not raise any exceptions" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new SuccessFunction

    // Act
    val response = lambdaFunction.handle(request, context)
    val body = response.getBody.jsonParse[ErrorMessage]

    // Assert
    body.message should be ("success")
  }
}