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

import scala.util.{Failure, Success, Try}

abstract class ServerlessHandler[TRequest, TResponse] {

  //--- Fields ---
  protected var lambdaContext: Context = _

  protected lazy val accountId: String = lambdaContext.getInvokedFunctionArn.split(':')(4)

  //--- Methods ---
  def handleRequest(request: TRequest): TResponse

  def handleFailure(e: Throwable): TResponse

  def handle(request: TRequest, context: Context): TResponse = {
    Console.println(s"Java Version: ${System.getProperty("java.version")}")
    lambdaContext = context
    Try(handleRequest(request)) match {
      case Success(response) => response
      case Failure(e: Throwable) => handleFailure(e)
    }
  }
}
