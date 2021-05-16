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
package handler.cors

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync
import com.amazonaws.services.simplesystemsmanagement.model._
import io.onema.userverless.config.cors.SsmCorsConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class SsmCorsConfigurationTest extends FlatSpec with Matchers with MockFactory {

  "Ssm CORS config" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue("test.com"))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result)
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isEnabled = ssmConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "Ssm CORS config with no stage name" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = new GetParameterRequest().withName("/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/cors/sites").withValue("https://foo.com"))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result)
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "")

    // Act
    val isEnabled = ssmConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "Ssm CORS config" should "return false when cors config is NOT enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).throws(new ParameterNotFoundException("Parameter does not exist")).anyNumberOfTimes()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isEnabled = ssmConfig.isEnabled
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isEnabled should be (false)
    isOriginValid should be (false)
  }

  "Ssm CORS config" should "return true when origin is valid" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue(originSite.get))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result).noMoreThanTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Ssm CORS config" should "return true when configured origin is *" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue("*"))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result).noMoreThanTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Ssm CORS config with multiple values" should "return true when origin is in configured values" in {

    // Arrange
    val configuredOriginValues = "https://foo.com,https://bar.com,http://baz.com"
    val originSite = Option("https://foo.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue(configuredOriginValues))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result).anyNumberOfTimes()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isEnabled = ssmConfig.isEnabled
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isEnabled should be (true)
    isOriginValid should be (true)
  }

  "Ssm CORS config with multiple values" should "return false when origin is not in configured values" in {

    // Arrange
    val configuredOriginValues = "https://foo.com,https://bar.com,http://baz.com"
    val originSite = Option("http://blah.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue(configuredOriginValues))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result).atLeastTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "Ssm CORS config with no values" should "return false for an origin" in {

    // Arrange
    val configuredOriginValues = ""
    val originSite = Option("http://blah.com")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue(configuredOriginValues))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result).atLeastTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "Ssm CORS config with no values" should "return false for no origin" in {

    // Arrange
    val configuredOriginValues = ""
    val originSite = Option("")
    val request = new GetParameterRequest().withName("/test/cors/sites").withWithDecryption(true)
    val result = new GetParameterResult().withParameter(new Parameter().withName("/test/cors/sites").withValue(configuredOriginValues))
    val ssmClientMock = mock[AWSSimpleSystemsManagementAsync]
    (ssmClientMock.getParameter _).expects(request).returning(result).atLeastTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }
}
