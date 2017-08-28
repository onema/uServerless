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
import org.scalatest.{FlatSpec, Matchers}
import onema.serverlessbase.core.json.Implicits.JsonStringToCaseClass
import onema.serverlessbase.model.ErrorMessage

class JsonParseToObjectTest extends FlatSpec with Matchers {
  "An Exception" should "generate a valid response" in {

    // Arrange
    val message = "{\"message\": \"foo bar\"}"

    // Act
    val errorMessage = message.jsonParse[ErrorMessage]

    // Assert
    errorMessage.message should be ("foo bar")
  }
}
