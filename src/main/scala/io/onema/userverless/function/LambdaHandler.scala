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

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.Logger
import io.onema.json.JavaExtensions._
import io.onema.json.Mapper
import io.onema.userverless.configuration.lambda.LambdaConfiguration
import io.onema.userverless.exception.ThrowableExtensions._
import io.onema.userverless.model.WarmUpEvent

import scala.io.Source
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import scala.collection.mutable.ArrayBuffer

abstract class LambdaHandler[TEvent:ClassTag, TResponse<: Any] extends LambdaConfiguration {

  //--- Fields ---
  protected val log: Logger = Logger("lambda-handler")

  protected val region: Regions = Regions.fromName(sys.env.getOrElse("AWS_REGION", "us-east-1"))

  private val snsErrorTopic = getValue("/sns/error/topic")

  protected lazy val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.standard().withRegion(region).build()

  protected val responseListeners: ArrayBuffer[TResponse => TResponse] = ArrayBuffer[TResponse => TResponse]()

  protected val validationListeners: ArrayBuffer[TEvent => Unit] = ArrayBuffer[TEvent => Unit]()

  //--- Methods ---
  def execute(event: TEvent, context: Context): TResponse

  def lambdaHandler(inputStream: InputStream, outputStream: OutputStream, context: Context): Unit = {
    val json = Source.fromInputStream(inputStream).mkString
    log.info(json)

    // A schedule event for the lambda handler is considered a warm-up event and will return immediately
    if(!isWarmUpEvent(json)) {
      val event = decodeEvent(json)
      val response = handle {
        validationListeners.foreach(listener => listener(event))
        execute(event, context)
      }
      response.foreach(response => {
        val output = responseListeners.foldLeft(response)((res, func) => func(res))

        output match {
          case _: String =>

            // Strings should be treated independent from other objects to prevent format issues
            outputStream.write(output.asInstanceOf[String].getBytes(Charset.defaultCharset()))
          case _: java.lang.Number =>

            // AnyVal should be converted to string
            outputStream.write(output.toString.getBytes(Charset.defaultCharset()))
          case _: AnyRef =>

            // Ensure that only AnyRef objects are converted to json
            outputStream.write(output.asInstanceOf[AnyRef].asJson.getBytes(Charset.defaultCharset()))
        }
      })
    }
    outputStream.close()
  }

  protected def handle(function: => TResponse): Option[TResponse] = {
    Try(function) match {
      case Success(response) =>

        // If the TResponse is Unit, return None, else wrap the response in an option
        val returnVal = response match {
          case _: Unit => None
          case _ => Option(response)
        }
        returnVal

      case Failure(e: Throwable) => Option(handleFailure(e))
    }
  }

  protected def handleFailure(exception: Throwable): TResponse = {
    val message = exception.message
    log.error(message)
    snsErrorTopic.foreach(snsClient.publish(_, message))
    throw exception
  }

  protected def isWarmUpEvent(json: String): Boolean = {
    log.debug("Warmup Event")
    time {
      Try(json.jsonDecode[WarmUpEvent]) match {
        case Success(event) =>
          log.info("Checking for warm up event!")
          event.getWarmup
        case Failure(exception) =>
          false
      }
    }
  }

  protected def decodeEvent(json: String): TEvent = {
    log.debug("Decode Event")
    time {
      if(json.isEmpty) {
        return tryCastEmpty(json)
      }
      val mapper: ObjectMapper = Mapper.allowUnknownPropertiesMapper
      Try(json.jsonDecode[TEvent](mapper)) match {
        case Success(event) => event
        case Failure(e) =>
          log.error(s"Unable to parse json message to expected type")
          handleFailure(e)
          throw e
      }
    }
  }

  protected def tryCastEmpty(str: String): TEvent = {
    Try(str.asInstanceOf[TEvent]) match {
      case Success(value) => value
      case Failure(e) =>
        log.error(s"Unable to properly cast empty value: ${e.getMessage}")
        handleFailure(e)
        throw e
    }
  }


  protected def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    log.info("Elapsed time: " + (t1 - t0)/1000000 + "milliseconds")
    result
  }

  protected def responseListener(listener: TResponse => TResponse): Unit = {
    responseListeners += listener
  }

  protected def validationListener(listener: TEvent => Unit): Unit = {
    validationListeners += listener
  }
}
