/**
  * This file is part of the ONEMA onema Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.function

import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.typesafe.scalalogging.Logger
import onema.serverlessbase.exception.ThrowableExtensions._

import scala.util.{Failure, Success, Try}

trait SnsHandler {

  //--- Methods ---
  protected def log = Logger("sns-handler")

  private val snsErrorTopic = sys.env.get("SNS_ERROR_TOPIC")

  private val region = sys.env.get("AWS_REGION")

  private val snsClient = AmazonSNSAsyncClientBuilder.defaultClient()

  protected def handle(function: () => Unit): Unit = {
    Try(function()) match {
      case Success(_) =>
      case Failure(e: Throwable) => handleFailure(e)
    }
  }

  protected def handleFailure(exception: Throwable): Unit = {
    val message = exception.message
    log.error(message)
    if(snsErrorTopic.isDefined) {
      // report error to SNS Topic
      snsClient.publish(snsErrorTopic.get, message)
    }
    throw exception
  }
}
