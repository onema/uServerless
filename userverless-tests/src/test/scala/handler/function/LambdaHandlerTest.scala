/**
  * This file is part of the ONEMA lambda-mailer Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package handler.function

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import functions._
import handler.EnvironmentHelper
import io.onema.userverless.exception.MessageDecodingException
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class LambdaHandlerTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {

  "An SNS Message as an input stream" should "get decoded to an SNSMessageEvent" in {
    // Arrange
    val message = "{\n  \"account\": \"123456789012\",\n  \"region\": \"us-east-1\",\n  \"detail\": {},\n  \"detail-type\": \"Scheduled Event\",\n  \"source\": \"aws.events\",\n  \"time\": \"1970-01-01T00:00:00Z\",\n  \"id\": \"cdc73f9d-aea9-11e3-9d5a-835b769c0d9c\",\n  \"resources\": [\n    \"arn:aws:events:us-east-1:123456789012:rule/my-schedule\"\n  ]\n}"
    val inputStream = new ByteArrayInputStream(message.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val function = new process.ScheduledFunction()

    // Act
    function.lambdaHandler(inputStream, outputStream, context)
    val response = outputStream.toString()


    // Assert
    response should be ("true")
  }

  "A simple service that takes an Int" should "Throw an exception on wrong value value" in {
    // Arrange
    val function = new simple.EchoFunction
    val number = ""
    val inputStream = new ByteArrayInputStream(number.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act-Assert
    assertThrows[MessageDecodingException] {
      function.lambdaHandler(inputStream, outputStream, context)
    }
  }

  "A simple service returning a String" should "properly write the value to the output stream" in {
    // Arrange
    val function = new simple.EchoFunction
    val number = "123"
    val inputStream = new ByteArrayInputStream(number.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    function.lambdaHandler(inputStream, outputStream, context)
    val response = outputStream.toString()

    // Assert
    response.foreach(x => x.isDigit should be(true))
  }

  "A simple service returning an integer" should "properly write the integer to the output stream" in {
    // Arrange
    val function = new simple.PositiveRandomFunction
    val inputStream = new ByteArrayInputStream("0".getBytes)
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act
    function.lambdaHandler(inputStream, outputStream, context)
    val response = outputStream.toString()

    // Assert
    response.foreach(x => x.isDigit should be(true))
  }
  "A bad request object" should "Throw an exception" in {
    // Arrange
    val function = new simple.EchoFunction
    val number = "{}"
    val inputStream = new ByteArrayInputStream(number.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext

    // Act-Assert
    assertThrows[MessageDecodingException] {
      function.lambdaHandler(inputStream, outputStream, context)
    }

    // Assert
  }

  // @TODO: This is a misleading test. The serialization needs to be tuned to properly handle all cases.
  // Currently it is too lose and serialization would not fail in most instances
//  "A service with an invalid event type" should "throw an exception" in {
//    // Arrange
//    val lambdaFunction = new ScheduledFunction()
//    val output = new ByteArrayOutputStream()
//    val context = new MockLambdaContext
//
//    // Act - Assert
//    intercept[Exception] {
//      lambdaFunction.lambdaHandler(toInputStream(""), output, context)
//    }
//  }
}
