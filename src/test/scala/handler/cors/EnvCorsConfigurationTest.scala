/**
  * This file is part of the ONEMA io.onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */
package handler.cors

import handler.EnvironmentHelper
import io.onema.serverlessbase.configuration.cors.EnvCorsConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class TestEnvCorsConfiguration(sites: Option[String], origin: Option[String]) extends EnvCorsConfiguration(origin) {
  override val corsSites: Option[String] = sites
}

class EnvCorsConfigurationTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {


  "Env CORS configuration" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Some("https://foo.com")
    val envConfig = new TestEnvCorsConfiguration(Some("*"), originSite)

    // Act
    val isEnabled = envConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "Env CORS configuration" should "return false when cors config is NOT enabled" in {

    // Arrange
    val originSite = Some("https://foo.com")
    val envConfig = new TestEnvCorsConfiguration(None, originSite)

    // Act
    val isEnabled = envConfig.isEnabled
    val isValidOrigin = envConfig.isOriginValid

    // Assert
    isEnabled should be (false)
    isValidOrigin should be (false)
  }

  "Env CORS configuration" should "return true when origin is valid" in {

    // Arrange
    val originSite = Some("https://foo.com")
    val envConfig = new TestEnvCorsConfiguration(originSite, originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Env CORS configuration" should "return true when configured origin is *" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val envConfig = new TestEnvCorsConfiguration(Some("*"), originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Env CORS configuration with multiple values" should "return true when origin is in configured values" in {

    // Arrange
    val configuredOriginValues = Some("https://foo.com,https://bar.com,http://baz.com")
    val originSite = Some("http://baz.com")
    val envConfig = new TestEnvCorsConfiguration(configuredOriginValues, originSite)

    // Act
    val isEnabled = envConfig.isEnabled
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isEnabled should be (true)
    isOriginValid should be (true)
  }

  "Env CORS configuration with multiple values" should "return false when origin is not in configured values" in {

    // Arrange
    val configuredOriginValues = Some("https://foo.com,https://bar.com,http://baz.com")
    val originSite = Some("http://blah.com")
    val envConfig = new TestEnvCorsConfiguration(configuredOriginValues, originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "Env CORS configuration with no values" should "return false for an origin" in {

    // Arrange
    val configuredOriginValues = Some("")
    val originSite = Some("http://blah.com")
    val envConfig = new TestEnvCorsConfiguration(configuredOriginValues, originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "Env CORS configuration with no values" should "return false for no origin" in {

    // Arrange
    val configuredOriginValues = Some("")
    val originSite = Option("")
    val envConfig = new TestEnvCorsConfiguration(configuredOriginValues, originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }
}
