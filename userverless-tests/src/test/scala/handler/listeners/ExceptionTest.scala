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

package handler.listeners

import java.io.ByteArrayOutputStream

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import functions.error.{ExceptionReporter, FunctionWithListener}
import io.onema.userverless.test.TestJavaObjectExtensions._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ExceptionTest extends FlatSpec with Matchers with MockFactory {

  "A validation function " should "return a bad request exception" in {

    // Arrange
    val event = "\"something is not right!!\""
    val context = new MockLambdaContext
    val reporterMock = mock[ExceptionReporter]
    (reporterMock.report _).expects(*).once()
    val lambdaFunction = new FunctionWithListener(reporterMock)
    val output = new ByteArrayOutputStream()

    // Act - Assert
    assertThrows[RuntimeException] {
      lambdaFunction.lambdaHandler(event.toInputStream, output, context)
    }
  }
}
