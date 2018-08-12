/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.serverlessbase.function

import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.json.JavaExtensions._
import io.onema.serverlessbase.configuration.lambda.LambdaConfiguration
import io.onema.serverlessbase.exception.ThrowableExtensions._

import scala.io.Source
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

abstract class LambdaHandler[TEvent:ClassTag, TResponse<: Any] extends LambdaConfiguration {

  //--- Fields ---
  protected val log: Logger = Logger("lambda-handler")

  protected val region: Regions = Regions.fromName(sys.env.getOrElse("AWS_REGION", "us-east-1"))

  private val snsErrorTopic = getValue("/sns/error/topic")

  protected lazy val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.standard().withRegion(region).build()

  //--- Methods ---
  def execute(event: TEvent, context: Context): TResponse

  def lambdaHandler(inputStream: InputStream, outputStream: OutputStream, context: Context): Unit = {
    val json = Source.fromInputStream(inputStream).mkString
    log.info(json)
    val event = json.jsonDecode[TEvent]
    val response = handle {
      execute(event, context)
    }
    response.foreach(response => {
      outputStream.write(response.asJson.getBytes(Charset.defaultCharset()))
    })
    outputStream.close()
  }

  protected def handle(function: => TResponse): Option[TResponse] = {
    Try(function) match {
      case Success(response) =>

        // If the TResponse is Unit, return None, else wrap the response in an option
        val returnVal = response match {
          case _:Unit => None
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
}
