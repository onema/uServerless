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

import onema.core.json.Implicits._
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import org.scalatest.{FlatSpec, Matchers}


class HandleRequestExceptionTest extends FlatSpec with Matchers {

  "An Exception" should "generate a valid response" in {

    // Arrange
    val exception = new HandleRequestException(code = 400, message = "there was an error")

    // Act
    val errorResponse = exception.httpResponse
    val parsedMessage = errorResponse.getBody.jsonParse[ErrorMessage]

    // Assert
    errorResponse.getBody should be ("{\"message\":\"there was an error\"}")
    parsedMessage.message should be ("there was an error")
  }
}
