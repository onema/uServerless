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

import cats.effect.IO
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.typesafe.scalalogging.Logger
import io.onema.userverless.common.ConfigExtensions.AwsProxyResponseExtension
import io.onema.userverless.exception.HandleRequestException
import io.onema.userverless.common.AwsProxyExtensions.AwsProxyRequestExtensions
import io.onema.userverless.common.HttpStatus
import io.onema.userverless.common.LogExtensions.LoggerExtensions
import io.onema.userverless.config.{Configuration, CorsConfiguration, NoopCorsConfiguration}
import io.onema.userverless.monitoring.LogMetrics.count


trait ApiGatewayHandler extends LambdaHandler[AwsProxyRequest, AwsProxyResponse]
  with ApiGatewayResponseBuilder
  with Configuration {

  //--- Fields ---
  override protected val log: Logger = Logger(classOf[ApiGatewayHandler])

  //--- Methods ---

  /**
    * Custom handleFailure method for ApiGateway, will generate a response with a custom error message or a generic
    * message to prevent exposing internal details.
    *
    * @param exception the reported exception
    * @return TResponse
    */
  override protected def handleFailure(exception: Throwable, reportException: Boolean): IO[AwsProxyResponse] = {
    log.error(exception)
    exception match {

      // Handled Exceptions generate a response with an error message.
      // This is well suited for 4XX errors and should not be reported
      case ex: HandleRequestException => for {
        _ <- IO { count("uServerlessApiGatewayHandledError") }
        response <- IO { buildError(ex.code, ex.getMessage) }
      } yield response

      // General exception, handle it gracefully
      case ex => for {
        _ <- IO { log.error(ex, reportException) }
        _ <- IO { count("uServerless.functionError") }
        response <- IO {
          buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: check the logs for more information.")
        }
      } yield response
    }
  }
}

object ApiGatewayHandler {

  /**
    * Trait for functions that require cors
    */
  trait Cors {

    //--- Methods ---
    /**
      * This method should construct and return the the cors config
      * @param origin option containing a string or None with the request origin.
      * @return CorsConfiguration
      */
    protected def corsConfiguration(origin: Option[String]): CorsConfiguration

    /**
      * Curried method that takes the API Gateway AwsProxyRequest as it's first parameter. Before
      * executing the codeBlock passed to it, it check if the config and the origin are valid.
      * @param request the AWS proxy request
      * @return AwsProxyResponse
      */
    protected def cors(request: AwsProxyRequest)(function: => AwsProxyResponse): AwsProxyResponse = {
      val origin = request.origin
      val corsConfig = corsConfiguration(origin)
      corsConfig match {
        case _:NoopCorsConfiguration =>
          throw new RuntimeException(s"The CORS config ${NoopCorsConfiguration.getClass} is only a placeholder and " +
            s"should not be used. Consider using one of the available CORS Configuration strategies. " +
            s"For more information see the documentation."
          )
        case _ =>
      }
      if (!corsConfig.isOriginValid) {
        throw new HandleRequestException(HttpStatus.FORBIDDEN, s"Origin '${origin.getOrElse("")}' is not authorized")
      }
      function.withCors(corsConfig)
    }
  }
}
