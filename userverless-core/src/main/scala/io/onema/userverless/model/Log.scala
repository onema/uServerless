/**
  * This file is part of the ONEMA uServerless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.model

import java.text.SimpleDateFormat
import java.util.Calendar

import io.onema.userverless.extensions.ThrowableExtensions.getValue
import org.json4s.FieldSerializer
import org.json4s.FieldSerializer._

object Log {
  case class LogErrorMessage(
    message: String,
    cause: String,
    exceptionClass: String,
    stackTrace: Seq[StackTraceElement],
    timestamp: String = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(Calendar.getInstance().getTime),
    messageType: String = "exception",
    stage: String = getValue("STAGE_NAME").getOrElse("STAGE_NAME_IS_UNDEFINED"),
    appName: String = getValue("APP_NAME").getOrElse("APP_NAME_IS_UNDEFINED"),
    function: String = getValue("AWS_LAMBDA_FUNCTION_NAME").getOrElse("AWS_LAMBDA_FUNCTION_NAME_IS_UNDEFINED"),
    lambdaVersion: String = getValue("AWS_LAMBDA_FUNCTION_VERSION").getOrElse("AWS_LAMBDA_FUNCTION_VERSION_IS_UNDEFINED"),
    reportException: Boolean = true
  )

  case class StackTraceElement(fileName: String, lineNumber: Int, className: String, methodName: String)

  case class LogMessage(
     timestamp: String,
     version: String,
     message: String,
     loggerName: String,
     threadName: String,
     level: String,
     levelValue: Double,
     messageType: String,
     stage: String,
     appName: String,
     function: String,
     lambdaVersion: String
  )

  object Rename {

    //--- Methods ---
    def errorMessage: FieldSerializer[LogErrorMessage] = {
      FieldSerializer[LogErrorMessage](
        renameTo("timestamp", "@timestamp"),
        renameFrom("@timestamp", "timestamp")
      )
    }

    def logMessage: FieldSerializer[LogMessage] = {
      FieldSerializer[LogMessage](
        renameTo("timestamp", "@timestamp")
        orElse renameTo("version", "@version")
        orElse renameTo("loggerName", "logger_name")
        orElse renameTo("threadName", "thread_name")
        orElse renameTo("levelValue", "level_value"),
        renameFrom("@timestamp", "timestamp")
        orElse renameFrom("@version", "version")
        orElse renameFrom("logger_name", "loggerName")
        orElse renameFrom("thread_name", "threadName")
        orElse renameFrom("level_value", "levelValue")
      )
    }
  }
}
