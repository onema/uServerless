/**
  * This file is part of the ONEMA onema Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

import com.google.gson.Gson
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import org.scalatest.{FlatSpec, Matchers}


class HandleRequestExceptionTest extends FlatSpec with Matchers {

  "An Exception" should "generate a valid response" in {

    // Arrange
    val exception = new HandleRequestException(code = 400, message = "there was an error")
    val gson = new Gson()

    // Act
    val errorResponse = exception.httpResponse
    val parsedMessage = gson.fromJson(errorResponse.getBody, classOf[ErrorMessage])

    // Assert
    errorResponse.getBody should be ("{\"message\":\"there was an error\"}")
    parsedMessage.message should be ("there was an error")
  }
}
