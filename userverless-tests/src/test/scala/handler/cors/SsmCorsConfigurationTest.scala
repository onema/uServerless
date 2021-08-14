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

import io.onema.userverless.config.cors.SsmCorsConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.{GetParameterRequest, GetParameterResponse, Parameter, ParameterNotFoundException}

class SsmCorsConfigurationTest extends AnyFlatSpec with Matchers with MockFactory {

  "Ssm CORS config" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value("test.com").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result)
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isEnabled = ssmConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "Ssm CORS config with no stage name" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = GetParameterRequest.builder().name("/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/cors/sites").value("https://foo.com").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result)
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "")

    // Act
    val isEnabled = ssmConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "Ssm CORS config" should "return false when cors config is NOT enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).throws(ParameterNotFoundException.builder().build()).anyNumberOfTimes()
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
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value(originSite.get).build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result).noMoreThanTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Ssm CORS config" should "return true when configured origin is *" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value("*").build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result).noMoreThanTwice()
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
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value(configuredOriginValues).build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result).anyNumberOfTimes()
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
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value(configuredOriginValues).build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result).atLeastTwice()
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
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value(configuredOriginValues).build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result).atLeastTwice()
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
    val request = GetParameterRequest.builder().name("/test/cors/sites").withDecryption(true).build()
    val parameter = Parameter.builder().name("/test/cors/sites").value(configuredOriginValues).build()
    val result = GetParameterResponse.builder().parameter(parameter).build()
    val ssmClientMock = mock[SsmClient]
    (ssmClientMock.getParameter(_: GetParameterRequest)).expects(request).returning(result).atLeastTwice()
    val ssmConfig = SsmCorsConfiguration(originSite, ssmClientMock, "test")

    // Act
    val isOriginValid = ssmConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }
}
