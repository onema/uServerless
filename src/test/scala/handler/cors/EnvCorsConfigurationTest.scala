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
package handler.cors

import handler.EnvironmentHelper
import onema.serverlessbase.configuration.cors.EnvCorsConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class EnvCorsConfigurationTest extends FlatSpec with Matchers with MockFactory with EnvironmentHelper {


  "Env CORS configuration" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    setEnv("CORS_SITES", "*")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isEnabled = envConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "Env CORS configuration" should "return false when cors config is NOT enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    deleteEnv("CORS_SITES")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isEnabled = envConfig.isEnabled
    val isValidOrigin = envConfig.isOriginValid

    // Assert
    isEnabled should be (false)
    isValidOrigin should be (false)
  }

  "Env CORS configuration" should "return true when origin is valid" in {

    // Arrange
    val originSite = Option("https://foo.com")
    setEnv("CORS_SITES", originSite.get)
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Env CORS configuration" should "return true when configured origin is *" in {

    // Arrange
    val originSite = Option("https://foo.com")
    setEnv("CORS_SITES", "*")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  "Env CORS configuration with multiple values" should "return true when origin is in configured values" in {

    // Arrange
    val configuredOriginValues = "https://foo.com,https://bar.com,http://baz.com"
    setEnv("CORS_SITES", configuredOriginValues)
    val originSite = Option("http://baz.com")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isEnabled = envConfig.isEnabled
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isEnabled should be (true)
    isOriginValid should be (true)
  }

  "Env CORS configuration with multiple values" should "return false when origin is not in configured values" in {

    // Arrange
    val configuredOriginValues = "https://foo.com,https://bar.com,http://baz.com"
    setEnv("CORS_SITES", configuredOriginValues)
    val originSite = Option("http://blah.com")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "Env CORS configuration with no values" should "return false for an origin" in {

    // Arrange
    val configuredOriginValues = ""
    setEnv("CORS_SITES", configuredOriginValues)
    val originSite = Option("http://blah.com")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "Env CORS configuration with no values" should "return false for no origin" in {

    // Arrange
    val configuredOriginValues = ""
    setEnv("CORS_SITES", configuredOriginValues)
    val originSite = Option("")
    val envConfig = new EnvCorsConfiguration(originSite)

    // Act
    val isOriginValid = envConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }
}
