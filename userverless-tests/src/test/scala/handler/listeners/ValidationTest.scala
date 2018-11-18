/**
  * This file is part of the ONEMA userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package handler.listeners

import java.io.ByteArrayOutputStream

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import functions.validation.Function
import io.onema.userverless.test.TestJavaObjectExtensions._
import org.apache.http.HttpStatus
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class ValidationTest extends FlatSpec with Matchers with MockFactory {

  "A validation function " should "return a bad request exception" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new Function
    val output = new ByteArrayOutputStream()
    request.setHeaders(Map("Origin" -> "Foo.co").asJava)

    // Act
    lambdaFunction.lambdaHandler(request.toInputStream, output, context)
    val response: AwsProxyResponse = output.toObject[AwsProxyResponse]
    val body = response.getBody.toErrorMessage

    // Assert
    body.message should be ("The Body of your request is empty")
    response.getStatusCode should be (HttpStatus.SC_BAD_REQUEST)
  }

  "A validation function " should "return a another bad request exception" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new Function
    val output = new ByteArrayOutputStream()
    request.setBody("""{"message":"foobar"}""")

    // Act
    lambdaFunction.lambdaHandler(request.toInputStream, output, context)
    val response: AwsProxyResponse = output.toObject[AwsProxyResponse]
    val body = response.getBody.toErrorMessage

    // Assert
    body.message should be ("The Origin header is required")
    response.getStatusCode should be (HttpStatus.SC_UNAUTHORIZED)
  }

  "A validation function " should "return not fail when validation succeeds" in {

    // Arrange
    val request = new AwsProxyRequest
    val context = new MockLambdaContext
    val lambdaFunction = new Function
    val output = new ByteArrayOutputStream()
    request.setBody("""{"message":"foobar"}""")
    request.setHeaders(Map("Origin" -> "Foo.co").asJava)

    // Act
    lambdaFunction.lambdaHandler(request.toInputStream, output, context)
    val response: AwsProxyResponse = output.toObject[AwsProxyResponse]

    // Assert
    response.getBody should be ("""{"message":"validation succeeded"}""")
    response.getStatusCode should be (HttpStatus.SC_ACCEPTED)
  }
}


