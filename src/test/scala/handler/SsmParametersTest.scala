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
package handler

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync
import com.amazonaws.services.simplesystemsmanagement.model._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import SsmParametersTest.TestFunction
import onema.serverlessbase.configuration.lambda.SsmLambdaConfiguration

object SsmParametersTest {
  class TestFunction(ssmClientMock: AWSSimpleSystemsManagementAsync) extends SsmLambdaConfiguration {
    override protected val ssmClient: AWSSimpleSystemsManagementAsync = ssmClientMock
  }
}

class SsmParametersTest extends FlatSpec with BeforeAndAfter with Matchers with MockFactory with EnvironmentHelper {

  before {
    deleteEnv("STAGE_NAME")
  }

  "A function with ssm parameter store" should "return single parameters" in {

    // Arrange
    setEnv("STAGE_NAME", "test")
    val request = new GetParameterRequest().withName("/test/foo").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/foo").withValue("test value"))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result)
    val lambdaFunction = new TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValue("/foo")

    // Assert
    response.get should be ("test value")
  }

  "A function with ssm parameter store" should "return multiple parameters" in {

    // Arrange
    setEnv("STAGE_NAME", "test")
    val request = new GetParametersByPathRequest().withPath("/test/foo").withRecursive(true).withWithDecryption(true)
    val result = new GetParametersByPathResult().withParameters(
      new Parameter().withName("/test/foo").withValue("test foo value"),
      new Parameter().withName("/test/foo/bar").withValue("test bar value")
    )
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParametersByPath _).expects(request).returning(result)
    val lambdaFunction = new TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValues("foo")

    // Assert
    response("/foo") should be ("test foo value")
    response("/foo/bar") should be ("test bar value")
  }

  "A function with ssm parameter store and no environment name" should "return single parameters" in {

    // Arrange
    val request = new GetParameterRequest().withName("/foo").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/foo").withValue("test value"))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result)
    val lambdaFunction = new TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValue("/foo")

    // Assert
    response.get should be ("test value")
  }

  "A function with ssm parameter store that does not exist" should "return None" in {

    // Arrange
    val request = new GetParameterRequest().withName("/foo").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/foo").withValue("test value"))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request) throws new ParameterNotFoundException("message")
    val lambdaFunction = new TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValue("/foo")

    // Assert
    response should be (None)
  }

  "A function that throws an exception" should "throw re-throw exception" in {

    // Arrange
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(*) throws new RuntimeException("message")
    val lambdaFunction = new TestFunction(ssmClientMock)

    // Act - Assert
    intercept[RuntimeException] { lambdaFunction.getValue("/bad") }
  }

  "A function with ssm parameter store" should "return multiple parameters using recursion" in {

    // Arrange
    setEnv("STAGE_NAME", "test")
    val request = new GetParametersByPathRequest().withPath("/test/foo").withRecursive(true).withWithDecryption(true)
    val requestWithNextToken = new GetParametersByPathRequest().withPath("/test/foo")
      .withRecursive(true)
      .withWithDecryption(true)
      .withNextToken("FooBarBazToken")
    val result1 = new GetParametersByPathResult().withNextToken("FooBarBazToken").withParameters(
      new Parameter().withName("/test/foo").withValue("test foo value"),
      new Parameter().withName("/test/foo/bar").withValue("test bar value")
    )
    val result2 = new GetParametersByPathResult().withParameters(
      new Parameter().withName("/test/baz").withValue("test baz value"),
      new Parameter().withName("/test/blah").withValue("test blah value")
    )
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParametersByPath _).expects(request).returning(result1).noMoreThanOnce()
    (ssmClientMock.getParametersByPath _).expects(requestWithNextToken).returning(result2).noMoreThanOnce()
    val lambdaFunction = new TestFunction(ssmClientMock)

    // Act
    val response = lambdaFunction.getValues("foo")

    // Assert
    response("/foo") should be ("test foo value")
    response("/foo/bar") should be ("test bar value")
    response("/baz") should be ("test baz value")
    response("/blah") should be ("test blah value")
  }
}
