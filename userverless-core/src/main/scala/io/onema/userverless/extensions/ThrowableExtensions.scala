/**
  * This file is part of the ONEMA uServerless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2021, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.extensions

import io.onema.json.Extensions._
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.model.Log.{LogErrorMessage, Rename, StackTraceElement}

import java.io.{PrintWriter, StringWriter}

object ThrowableExtensions extends EnvLambdaConfiguration {

  implicit class ExceptionMessage(throwable: Throwable) {

    // --- Methods ---

    /**
      * Converts a class into a json string.
      */
    def message: String = {
      val sw = new StringWriter
      throwable.printStackTrace(new PrintWriter(sw))
      sw.toString
    }

    def structuredMessage(reportException: Boolean): String = {
      val stackTrace = throwable.getStackTrace.map(e => {
        StackTraceElement(e.getFileName, e.getLineNumber, e.getClassName, s"${e.getMethodName}()")
      })

      val cause = Option(throwable.getCause) match {
        case Some(e) => e.getMessage
        case None => "No cause available"
      }
      LogErrorMessage(throwable.getMessage, cause, throwable.getClass.getName, stackTrace, reportException = reportException)
        .asJson(Rename.errorMessage)
    }
  }
}
