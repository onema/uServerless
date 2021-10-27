/*
 * This file is part of the ONEMA userverless-core Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2018-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.userverless.service

import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.unsafe.implicits.global
import cats.effect.{Deferred, IO, OutcomeIO}
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.Logger
import io.onema.json.JavaExtensions._
import io.onema.json.Mapper
import io.onema.userverless.exception.{HandledException, MessageDecodingException}
import io.onema.userverless.common.LogExtensions.LoggerExtensions
import io.onema.userverless.config.Configuration
import io.onema.userverless.monitoring.LogMetrics.{count, timeIO, timeSignal}

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.reflect.ClassTag

abstract class LambdaHandler[TEvent: ClassTag, TResponse<: Any] extends Configuration {

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

    val lambdaProgram: IO[TResponse] = handle(outputStream) {

      for {
        json <- IO { Source.fromInputStream(inputStream).mkString }
        _ <- IO { log.debug(json) }
        event <- decodeEvent(json)

        // Validate the event object using custom listeners ???
        result <- timeIO("uServerless.functionCode") { execute(event, context) }

        // Transform the response using custom listeners ???
      } yield result
    }

    // Edge of the Universe!
    lambdaProgram.unsafeRunSync()
    outputStream.close()
  }


  /**
    * Execute the service code and handles errors using the functionHandler. It returns None if the response type
    * is Unit, otherwise it returns an Option of TResponse.
    *
    * @param codeBlock any codeBlock that returns a TResponse wrapped in an IO
    * @return IO[TResponse]
    */
  protected def handle(outputStream: OutputStream)(codeBlock: => IO[TResponse]): IO[TResponse] = {
    for {
      signal <- Deferred[IO, Unit]
      fibTimer <- timeSignal("uServerless.lambdaProgram")(signal).start

      fibCodeBlock <- codeBlock.start
      outcome <- fibCodeBlock.join
      result <- handleOutcome(outcome)
      _ <- transform(result, outputStream)

      _ <- signal.complete()
      _ <- fibTimer.join
    } yield result
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
    * General decoding handling, the decoding strategy is implemented in the jsonDecode method
    *
    * @param json String event
    * @return TEvent
    */
  private def decodeEvent(json: String): IO[TEvent] = {
    if (json.isEmpty) {
      IO.raiseError(new MessageDecodingException("Empty event values are not allowed"))
    } else {
      val jsonDecodeIO = IO { jsonDecode(json) }
      jsonDecodeIO.handleErrorWith { e =>
        log.warn(s"Unable to parse json message to expected type")
        IO.raiseError(new MessageDecodingException(e.getMessage))
      }
    }
  }


  protected def handleOutcome(outcome: OutcomeIO[TResponse]): IO[TResponse] = {
    outcome match {
      case Succeeded(fa) => fa
      case Errored(e: HandledException) =>  handleFailure(e, reportEx = false)
      case Errored(e: Throwable) =>  handleFailure(e, reportEx = true)
      case Canceled() => IO.raiseError(new RuntimeException("The lambda service code block was cancelled"))
    }
  }

  protected def transform[TR](output: TR, outputStream: OutputStream): IO[Unit] = {
    // Transform based on response type
    output match {
      // Do nothing
      case _: Unit => IO.unit

      // If is IO, unpack it
      case io: IO[TResponse] => for {
        result <- io
        _ <- transform(result, outputStream)
      } yield ()

      // Strings should be treated independent from other objects to prevent format issues
      case out: String => IO {
        outputStream.write(out.getBytes(Charset.defaultCharset()))
      }.void

      // Numbers should be converted to string
      case out: java.lang.Number => IO {
        outputStream.write(out.toString.getBytes(Charset.defaultCharset()))
      }.void

      // Ensure that only AnyRef objects are converted to json
      case out: AnyRef => IO {
        outputStream.write(out.asJson.getBytes(Charset.defaultCharset()))
      }.void
    }
  }

  /**
    * Deal with exceptions, the basic logic reports and re-throws the exception.
    *
    * @param exception the reported exception
    * @return TResponse
    */
  protected def handleFailure(exception: Throwable, reportEx: Boolean): IO[TResponse] = {
    log.error(exception, reportEx)
    count("uServerless.functionError")
    exceptionListeners.foreach(listener => listener(exception))
    IO.raiseError(exception)
  }
}
