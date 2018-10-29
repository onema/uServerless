/**
  * This file is part of the ONEMA uServerlessEvents Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

import org.scalatest.{FlatSpec, Matchers}
import io.onema.json.Extensions._
import io.onema.userverless.events.ApiGateway.AwsProxyRequest

import scala.io.Source

class ApiGatewayTest   extends FlatSpec with Matchers {

  "A API Gateway proxy request" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/api-gateway-proxy.json")).mkString

    // Act
    val model = json.jsonDecode[AwsProxyRequest]

    // Assert
    model.httpMethod.get should be("POST")
    model.headers.getOrElse("Accept", "") should be("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    model.pathParameters.get("proxy") should be("/path/to/resource")
    model.requestContext.authorizer.claims.email should be(None)
  }
  "A API Gateway request with cognito authorizer" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/api-gateway-with-cognito-authorizer.json")).mkString

    // Act
    val model = json.jsonDecode[AwsProxyRequest]

    // Assert
    model.httpMethod.get should be("POST")
    model.headers.getOrElse("accept", "") should be("*/*")
    model.headers.getOrElse("origin", "") should be("http://test.com")
    model.requestContext.authorizer.claims.`cognito:username`.get should be("test-at-email.com")
    model.requestContext.authorizer.claims.email.get should be("test@email.com")
  }

}
