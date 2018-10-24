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

package handler.warmup

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import functions.process._
import handler.EnvironmentHelper
import io.onema.userverless.events.Sns.{SnsEvent, SnsRecord, SnsRecords}
import io.onema.json.Extensions._
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.exception.MessageDecodingException
import io.onema.userverless.function.SnsHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class LambdaHandlerWarmUpTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {

  "A schedule event with an valid warmup event" should "return true" in {
    // Arrange
    val message = "{\"warmup\":true}"
    val inputStream = new ByteArrayInputStream(message.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val function = new Function()

    // Act
    function.lambdaHandler(inputStream, outputStream, context)

    // Assert
    outputStream.toString() should be ("")
  }

  "An SNS event " should "deserialize correctly" in {
    // Arrange
    val rand = scala.util.Random.nextDouble()
    class TestFunction extends SnsHandler[Double] with NoopLambdaConfiguration {
      def execute(event: Double, context: Context): Unit = {
        event should be (rand)
      }
    }
    val message = SnsEvent(List(SnsRecords(Sns = SnsRecord(Message = Some(rand.toString))))).asJson
    val inputStream = new ByteArrayInputStream(message.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val function = new TestFunction()

    // Act
    function.lambdaHandler(inputStream, outputStream, context)

    // Assert
    outputStream.toString() should be ("")
  }

  "An SNS event with invalid different type" should "serialize to expected type only" in {
    // Arrange
    val rand = scala.util.Random.nextDouble()
    class TestFunction extends SnsHandler[Int] with NoopLambdaConfiguration {
      def execute(event: Int, context: Context): Unit = {
        event should not be rand
      }
    }
    val message = SnsEvent(List(SnsRecords(Sns = SnsRecord(Message = Some(rand.toString))))).asJson
    val inputStream = new ByteArrayInputStream(message.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val function = new TestFunction()

    // Act - Assert
    function.lambdaHandler(inputStream, outputStream, context)
  }

  "An SNS event with no record " should "throw and exception" in {
    // Arrange
    val message = SnsEvent(List()).asJson
    val inputStream = new ByteArrayInputStream(message.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val function = new Function()

    // Act - Assert
    assertThrows[MessageDecodingException] {
      function.lambdaHandler(inputStream, outputStream, context)
    }
  }

  "A schedule event with a warmup event" should "return true" in {
    // Arrange
    val message = "{\n  \"account\": \"123456789012\",\n  \"region\": \"us-east-1\",\n  \"detail\": {},\n  \"detail-type\": \"Scheduled Event\",\n  \"source\": \"aws.events\",\n  \"time\": \"1970-01-01T00:00:00Z\",\n  \"id\": \"cdc73f9d-aea9-11e3-9d5a-835b769c0d9c\",\n  \"resources\": [\n    \"arn:aws:events:us-east-1:123456789012:rule/my-schedule\"\n  ]\n}"
    val inputStream = new ByteArrayInputStream(message.getBytes())
    val outputStream = new ByteArrayOutputStream()
    val context = new MockLambdaContext
    val function = new ScheduledFunction()

    // Act
    function.lambdaHandler(inputStream, outputStream, context)
    val response = outputStream.toString()

    // Assert
    response should be ("true")

  }
}
