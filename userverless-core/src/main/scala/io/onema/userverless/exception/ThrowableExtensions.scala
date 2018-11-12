/**
  * This file is part of the ONEMA onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.exception

import java.io.{PrintWriter, StringWriter}
import java.text.SimpleDateFormat
import java.util.Calendar

import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.json.Extensions._
import org.json4s.FieldSerializer
import org.json4s.FieldSerializer._

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

    def structuredMessage: String = {
      val stackTrace = throwable.getStackTrace.map(e => {
        StackTraceElement(e.getFileName, e.getLineNumber, e.getClassName, s"${e.getMethodName}()")
      })
      ErrorStructure(throwable.getMessage, throwable.getClass.getName, stackTrace).asJson(ErrorStructure.rename)
    }
  }

  case class ErrorStructure(
    message: String,
    exceptionClass: String,
    stackTrace: Seq[StackTraceElement],
    timestamp: String = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(Calendar.getInstance().getTime),
    messageType: String = "exception",
    stage: String = getValue("STAGE_NAME").getOrElse("STAGE_NAME_IS_UNDEFINED"),
    function: String = getValue("AWS_LAMBDA_FUNCTION_NAME").getOrElse("AWS_LAMBDA_FUNCTION_NAME_IS_UNDEFINED"),
    lambdaVersion: String = getValue("AWS_LAMBDA_FUNCTION_VERSION").getOrElse("AWS_LAMBDA_FUNCTION_VERSION_IS_UNDEFINED")
  )

  object ErrorStructure {
    def rename: FieldSerializer[ErrorStructure] = {
      FieldSerializer[ErrorStructure](renameTo("messageType", "type") orElse renameTo("timestamp", "@timestamp"))
    }
  }

  case class StackTraceElement(fileName: String, lineNumber: Int, className: String, methodName: String)

}
