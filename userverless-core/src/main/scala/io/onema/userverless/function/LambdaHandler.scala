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

package io.onema.userverless.function

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.Logger
import io.onema.json.JavaExtensions._
import io.onema.json.Mapper
import io.onema.userverless.config.lambda.LambdaConfiguration
import io.onema.userverless.extensions.LogExtensions.LoggerExtensions
import io.onema.userverless.exception.{HandledException, MessageDecodingException}
import io.onema.userverless.domain.WarmUpEvent
import io.onema.userverless.monitoring.LogMetrics.{count, time}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

abstract class LambdaHandler[TEvent: ClassTag, TResponse<: Any] extends LambdaConfiguration {

  //--- Fields ---
  protected val log: Logger = Logger(classOf[LambdaHandler[TEvent, TResponse]])

  protected val region: String = sys.env.getOrElse("AWS_REGION", "us-east-1")

  protected val reportException: Boolean = sys.env.getOrElse("REPORT_EXCEPTION", "false").toBoolean

  protected val responseListeners: ArrayBuffer[TResponse => TResponse] = ArrayBuffer[TResponse => TResponse]()

  protected val validationListeners: ArrayBuffer[TEvent => Unit] = ArrayBuffer[TEvent => Unit]()

  protected val exceptionListeners: ArrayBuffer[Throwable => Unit] = ArrayBuffer[Throwable => Unit]()

  //--- Methods ---
  /**
    * This method should be implemented by all lambda functions a and is called by the lambda handler. Please note that
    * the main entry to the lambda codeBlock should be the "lambdaHandler".
    *
    * @param event TEvent
    * @param context AWS Context
    * @return TResponse, if not response is required set this to Unit
    */
  def execute(event: TEvent, context: Context): TResponse

  /**
    * The lambdaHandler is the main entry point to your application and it is in charge of decoding your event,
    * handling errors/notifications, and writing to the output stream.
    *
    * @param inputStream aws input stream
    * @param outputStream aws output stream
    * @param context aws lambda context
    */
  final def lambdaHandler(inputStream: InputStream, outputStream: OutputStream, context: Context): Unit = {
    val json = Source.fromInputStream(inputStream).mkString
    log.debug(json)

    // Check if it is a warm-up event and if it is, it will return immediately!
    if(!isWarmUpEvent(json)) {
      val response: Option[TResponse] = handle {

        // Decode the event
        val event = time("uServerlessJsonDecode") {
          decodeEvent(json)
        }

        // Validate the event object using custom listeners
        time("uServerlessFunctionValidation") {
          validationListeners.foreach(listener => listener(event))
        }

        // Execute the lambda function code for the application
        time("uServerlessFunctionCode") {
          execute(event, context)
        }
      }

      // If the response is not None, make the proper transformations before writing it to the output stream
      response.foreach(response => {

        // Transform the response using custom listeners
        val output = responseListeners.foldLeft(response)((res, func) => func(res))

        // Transform based on response type
        output match {

          // Strings should be treated independent from other objects to prevent format issues
          case _: String =>
            outputStream.write(output.asInstanceOf[String].getBytes(Charset.defaultCharset()))

          // Numbers should be converted to string
          case _: java.lang.Number =>
            outputStream.write(output.toString.getBytes(Charset.defaultCharset()))

          // Ensure that only AnyRef objects are converted to json
          case _: AnyRef =>
            outputStream.write(output.asInstanceOf[AnyRef].asJson.getBytes(Charset.defaultCharset()))
        }
      })
    }
    outputStream.close()
  }

  /**
    * Execute the function code and handles errors using the functionHandler. It returns None if the response type
    * is Unit, otherwise it returns an Option of TResponse.
    *
    * @param codeBlock any codeBlock that returns a TResponse
    * @return Option[TResponse]
    */
  protected def handle(codeBlock: => TResponse): Option[TResponse] = {
    Try(codeBlock) match {

      // If the Type is Unit, return None
      case Success(_: Unit) => None

      // Wrap a successful response in an Option
      case Success(response) => Option(response)

      // Handled exceptions will not be reported
      case Failure(e: HandledException) => Option(handleFailure(e, reportEx = false))

      // Other exception may be reported
      case Failure(e: Throwable) => Option(handleFailure(e, reportException))
    }
  }

  /**
    * Deal with exceptions, the basic logic reports and re-throws the exception.
    *
    * @param exception the reported exception
    * @return TResponse
    */
  protected def handleFailure(exception: Throwable, reportEx: Boolean): TResponse = {
    log.error(exception, reportEx)
    count("uServerlessFunctionError")
    exceptionListeners.foreach(listener => listener(exception))
    throw exception
  }

  /**
    * check if the message is a warm up event or not. Returns true if it is.
    *
    * @param json string event
    * @return Boolean
    */
  protected def isWarmUpEvent(json: String): Boolean = {
    time("uServerlessWarmUpEvent") {
      Try(json.jsonDecode[WarmUpEvent]) match {
        case Success(event) =>
          log.info("Checking for warm up event!")
          event.getWarmup
        case Failure(exception) =>
          false
      }
    }
  }

  /**
    * Override this method to use a custom json-decode strategy
    *
    * @param json string event
    * @return TEvent
    */
  protected def jsonDecode(json: String): TEvent = {
    val mapper: ObjectMapper = Mapper.allowUnknownPropertiesMapper
    json.jsonDecode[TEvent](mapper)
  }

  /**
    * Transform the function response using custom functions
    * @param listener function
    */
  protected def responseListener(listener: TResponse => TResponse): Unit = {
    responseListeners += listener
  }

  /**
    * Validate the request using custom functions
    * @param listener function
    */
  protected def validationListener(listener: TEvent => Unit): Unit = {
    validationListeners += listener
  }

  /**
    * Add custom logic in the handleFailure method before raising the exception
    * @param listener function
    */
  protected def exceptionListener(listener: Throwable => Unit): Unit = {
    exceptionListeners += listener
  }

  /**
    * General decoding handling, the decoding strategy is implemented in the jsonDecode method
    *
    * @param json String event
    * @return TEvent
    */
  private def decodeEvent(json: String): TEvent = {
    if(json.isEmpty) {
      throw new MessageDecodingException("Empty event values are not allowed")
    } else {
      Try(jsonDecode(json)) match {
        case Success(event) => event
        case Failure(e) =>
          log.warn(s"Unable to parse json message to expected type")
          throw new MessageDecodingException(e.getMessage)
      }
    }
  }
}
