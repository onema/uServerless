/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package io.onema.serverlessbase.function

import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSAsync
import com.typesafe.scalalogging.Logger
import io.onema.serverlessbase.configuration.lambda.LambdaConfiguration
import io.onema.serverlessbase.exception.ThrowableExtensions._

import scala.util.{Failure, Success, Try}

trait LambdaHandler[TResponse] extends LambdaConfiguration {

  //--- Fields ---
  protected val log: Logger = Logger("lambda-handler")

  protected val region: Regions = Regions.fromName(sys.env.getOrElse("AWS_REGION", "us-east-1"))

  private val snsErrorTopic = getValue("/sns/error/topic")

  protected val snsClient: AmazonSNSAsync

  //--- Methods ---
  protected def handle(function: => TResponse): TResponse = {
    Try(function) match {
      case Success(response) => response
      case Failure(e: Throwable) => handleFailure(e)
    }
  }

  protected def handleFailure(exception: Throwable): TResponse = {
    val message = exception.message
    log.error(message)
    if(snsErrorTopic.isDefined) {

      // report error to SNS Topic
      snsClient.publish(snsErrorTopic.get, message)
    }
    throw exception
  }
}
