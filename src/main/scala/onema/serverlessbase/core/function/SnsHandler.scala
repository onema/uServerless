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

package onema.serverlessbase.core.function

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

trait SnsHandler {

  //--- Fields ---
  val log = Logger("sns-handler")

  //--- Methods ---
  def handleRequest(request: SNSEvent): Unit

  def handle(request: SNSEvent, context: Context): Unit = {
    Try(handleRequest(request)) match {
      case Success(response) => response
      case Failure(e: Throwable) => handleFailure(e)
    }
  }
  def handleFailure(exception: Throwable): Unit = {
    log.error(exception.getStackTrace.mkString)
    log.error(exception.getMessage)
    throw exception
  }
}
