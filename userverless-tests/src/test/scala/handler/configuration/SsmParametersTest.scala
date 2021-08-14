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
package handler.configuration

import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxTuple3Semigroupal
import handler.EnvironmentHelper
import handler.configuration.SsmParametersTest.TestFunction
import io.onema.userverless.config.lambda.{SsmLambdaConfiguration, ValueIsNotANumber, ValueNotFound}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model._

import scala.language.postfixOps

object SsmParametersTest {
  case class TestFunction(override val ssmClient: SsmClient) extends SsmLambdaConfiguration
}

class SsmParametersTest extends AnyFlatSpec with BeforeAndAfter with Matchers with MockFactory with EnvironmentHelper {

  before {
    deleteEnv("STAGE_NAME")
  }

  "A function with ssm parameter store value and stage name" should "return single parameter" in {

    // Arrange
    deleteEnv("STAGE_NAME")
    setEnv("STAGE_NAME", "test")
    val request = GetParameterRequest.builder().name("/test/foo").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/foo").value("test value").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter (_: GetParameterRequest)).expects(request).returning(result)
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValue("/foo")

    // Assert
    response.get should be ("test value")
  }

  "A function with ssm parameter store value and stage name" should "validate and return a single parameter" in {

    // Arrange
    deleteEnv("STAGE_NAME")
    setEnv("STAGE_NAME", "test")
    val request = GetParameterRequest.builder().name("/test/foo").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/foo").value("test value").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter (_: GetParameterRequest)).expects(request).returning(result)
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.validate("/foo")

    // Assert
    response shouldBe a [Valid[_]]
    response.getOrElse("") should be ("test value")
  }

  "A function with mixed ssm parameters types, validation" should "return all values" in {

    // Arrange
    deleteEnv("STAGE_NAME")
    setEnv("STAGE_NAME", "test")
    val request1 = GetParameterRequest.builder().name("/test/foo").withDecryption(true).build()
    val request2 = GetParameterRequest.builder().name("/test/bar").withDecryption(true).build()
    val request3 = GetParameterRequest.builder().name("/test/baz").withDecryption(true).build()
    val parameter1 = Parameter.builder().name("/test/foo").value("test value").build()
    val parameter2 = Parameter.builder().name("/test/bar").value("1").build()
    val parameter3 = Parameter.builder().name("/test/baz").value("1.1").build()
    val result1 = GetParameterResponse.builder().parameter(parameter1).build()
    val result2 = GetParameterResponse.builder().parameter(parameter2).build()
    val result3 = GetParameterResponse.builder().parameter(parameter3).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request1).returning(result1).noMoreThanOnce()
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request2).returning(result2).noMoreThanOnce()
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request3).returning(result3).noMoreThanOnce()
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = (
      lambdaFunction.validate("/foo"),
      lambdaFunction.validateInt("/bar"),
      lambdaFunction.validateFloat("/baz")
      ).mapN(Tuple3[String, Int, Float])

    // Assert
    response shouldBe a [Valid[_]]
    response match {
      case Valid(tuple) =>
        tuple._1 should be ("test value")
        tuple._2 should be (1)
        tuple._3 should be (1.1F)
      case Invalid(e) => fail("Expected a valid configuration")
    }
  }

  "A function with ssm parameter store value and stage name" should "return multiple parameters" in {

    // Arrange
    deleteEnv("STAGE_NAME")
    setEnv("STAGE_NAME", "test")
    val request = GetParametersByPathRequest.builder().path("/test/foo").recursive(true).withDecryption(true).build()
    val result = GetParametersByPathResponse.builder().parameters(
      Parameter.builder().name("/test/foo").value("test foo value").build(),
      Parameter.builder().name("/test/foo/bar").value("test bar value").build()
    ).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParametersByPath(_: GetParametersByPathRequest)).expects(request).returning(result)
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValues("foo")

    // Assert
    response("/foo") should be ("test foo value")
    response("/foo/bar") should be ("test bar value")
  }

  "A function with ssm parameter store and no stage name" should "return single parameters" in {

    // Arrange

    val request = GetParameterRequest.builder().name("/foo").withDecryption(true).build()
    val parameter = Parameter.builder().name("/foo").value("test value").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter (_: GetParameterRequest)).expects(request).returning(result)
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValue("/foo")

    // Assert
    response.get should be ("test value")
  }

  "A function with ssm parameter store that does not exist" should "return None" in {

    // Arrange
    val request = GetParameterRequest.builder().name("/foo").withDecryption(true).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request).throws(ParameterNotFoundException.builder().build())
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValue("/foo")

    // Assert
    response should be (None)
  }

  "A function with ssm parameter store that does not exist, validation" should "return ValueNotFound" in {

    // Arrange
    val request = GetParameterRequest.builder().name("/foo").withDecryption(true).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request).throws(ParameterNotFoundException.builder().build())
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.validate("/foo")

    // Assert
    response shouldBe a [Invalid[_]]
  }

  "A function with multiple ssm parameters that do not exist, validation" should "return ValueNotFound" in {

    // Arrange
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(*).repeat(3).throws(ParameterNotFoundException.builder().build())
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response: lambdaFunction.ValidationResult[(String, String, String)] = (
      lambdaFunction.validate("/foo"),
      lambdaFunction.validate("/bar"),
      lambdaFunction.validate("/baz")
    ).mapN(Tuple3[String, String, String])

    // Assert
    response shouldBe a [Invalid[_]]
    response.map(e => e should be (ValueNotFound))
  }

  "A function with mixed ssm parameters that do and do not exist, validation" should "return ValueNotFound" in {

    // Arrang
    deleteEnv("STAGE_NAME")
    setEnv("STAGE_NAME", "test")
    val request = GetParameterRequest.builder().name("/test/foo").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/foo").value("test value").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request).returning(result).noMoreThanOnce()
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(*).throws(ParameterNotFoundException.builder().build()).noMoreThanTwice()
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response: lambdaFunction.ValidationResult[(String, String, String)] = (
      lambdaFunction.validate("/foo"),
      lambdaFunction.validate("/bar"),
      lambdaFunction.validate("/baz")
      ).mapN(Tuple3[String, String, String])

    // Assert
    response shouldBe a [Invalid[_]]
    response.map(e => e should be (ValueNotFound))
  }

  "A function with mixed invalid ssm parameters, validation with invalid types" should "return invalid values" in {

    // Arrange
    deleteEnv("STAGE_NAME")
    setEnv("STAGE_NAME", "test")
    val request1 = GetParameterRequest.builder().name("/test/foo").withDecryption(true).build()
    val request2 = GetParameterRequest.builder().name("/test/bar").withDecryption(true).build()
    val request3 = GetParameterRequest.builder().name("/test/baz").withDecryption(true).build()
    val parameter1 = Parameter.builder().name("/test/foo").value("bad").build()
    val parameter2 = Parameter.builder().name("/test/bar").value("bad").build()
    val parameter3 = Parameter.builder().name("/test/baz").value("bad").build()
    val result1 = GetParameterResponse.builder().parameter(parameter1).build()
    val result2 = GetParameterResponse.builder().parameter(parameter2).build()
    val result3 = GetParameterResponse.builder().parameter(parameter3).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request1).returning(result1).noMoreThanOnce()
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request2).returning(result2).noMoreThanOnce()
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(request3).returning(result3).noMoreThanOnce()
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = (
      lambdaFunction.validateDouble("/foo"),
      lambdaFunction.validateInt("/bar"),
      lambdaFunction.validateFloat("/baz")
      ).mapN(Tuple3[Double, Int, Float])

    // Assert
    response shouldBe a [Invalid[_]]
    response match {
      case Valid(tuple) => fail("Validation should have failed")
      case Invalid(errors) => errors.map(configValidation => configValidation shouldBe a [ValueIsNotANumber])
    }
  }

  "A function that throws an exception when getting a parameter value" should "throw re-throw the exception" in {

    // Arrange
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParameter(_: GetParameterRequest)).expects(*) throws new RuntimeException("message")
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act - Assert
    intercept[RuntimeException] { lambdaFunction.getValue("/bad") }
  }

  "A function with ssm parameter store" should "return multiple parameters using recursion" in {

    // Arrange
    setEnv("STAGE_NAME", "test")

    val nextToken = "FooBarBazToken"
    val request = GetParametersByPathRequest.builder().path("/test/foo").recursive(true).withDecryption(true).build()
    val requestWithNextToken = GetParametersByPathRequest.builder().path("/test/foo")
      .recursive(true)
      .withDecryption(true)
      .nextToken(nextToken)
      .build()
    val result1 = GetParametersByPathResponse.builder().nextToken(nextToken).parameters(
      Parameter.builder().name("/test/foo").value("test foo value").build(),
      Parameter.builder().name("/test/foo/bar").value("test bar value").build()
    ).build()
    val result2 = GetParametersByPathResponse.builder().parameters(
      Parameter.builder().name("/test/baz").value("test baz value").build(),
      Parameter.builder().name("/test/blah").value("test blah value").build()
    ).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock getParametersByPath(_: GetParametersByPathRequest)).expects(request).returning(result1).noMoreThanOnce()
    (ssmClientMock getParametersByPath(_: GetParametersByPathRequest)).expects(requestWithNextToken).returning(result2).noMoreThanOnce()
    val lambdaFunction = TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValues("foo")

    // Assert
    response("/foo") should be ("test foo value")
    response("/foo/bar") should be ("test bar value")
    response("/baz") should be ("test baz value")
    response("/blah") should be ("test blah value")
  }
}
