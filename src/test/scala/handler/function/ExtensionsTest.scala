/**
  * This file is part of the ONEMA userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package handler.function

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import handler.function.ExtensionsTest.MockContext
import io.onema.userverless.function.Extensions._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ExtensionsTest extends FlatSpec with Matchers with MockFactory {

  "The Context extension " should "return the AWS account" in {

    // Arrange
    val context = new MockContext

    // Act
    val accountId = context.accountId

    // Assert
    accountId.nonEmpty should be (true)
    accountId.get should be ("123456789012")
  }

  "A RichRegex " should "match a simple expression" in {

    // Arrange
    val regex = "^.+(userverless).+$".r

    // Act
    val result = regex matches "the userverless framework is awesome!"

    // Assert
    result should be (true)
  }

  "A RichRegex " should "match a complex expression" in {

    // Arrange
    // https://regex101.com/library/fX8dY0
    val passwordValidator = "^((?=\\S*?[A-Z])(?=\\S*?[a-z])(?=\\S*?[0-9]).{6,})\\S$".r

    // Act
    val validResult = passwordValidator matches "min6wordsOneUpperCaseonelowercaseAndANumber0"
    val invalidResult = passwordValidator matches "notvalidpassword"

    // Assert
    validResult should be (true)
    invalidResult should be (false)
  }
}

object ExtensionsTest {
  class MockContext() extends MockLambdaContext {
    override def getInvokedFunctionArn: String = "arn:aws:lambda:us-east-1:123456789012:function:function-name"
  }
}
