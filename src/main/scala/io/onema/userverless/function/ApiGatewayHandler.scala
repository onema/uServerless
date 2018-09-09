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

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.typesafe.scalalogging.Logger
import io.onema.json.JavaExtensions._
import io.onema.userverless.configuration.cors.{CorsConfiguration, NoopCorsConfiguration}
import io.onema.userverless.configuration.lambda.LambdaConfiguration
import io.onema.userverless.configuration.cors.Extensions.AwsProxyResponseExtension
import io.onema.userverless.exception.HandleRequestException
import io.onema.userverless.exception.ThrowableExtensions._
import org.apache.http.HttpStatus

import scala.io.Source
import scala.util.Try


trait ApiGatewayHandler extends LambdaHandler[AwsProxyRequest, AwsProxyResponse]
  with ApiGatewayResponse
  with LambdaConfiguration {

  //--- Fields ---
  override protected val log = Logger("apigateway-handler")

  //--- Methods ---
  protected def corsConfiguration(origin: Option[String]): CorsConfiguration = NoopCorsConfiguration()

  protected def cors(request: AwsProxyRequest)(function: => AwsProxyResponse): AwsProxyResponse = {
    val origin = Option(request.getHeaders.get("origin"))
    val corsConfig = corsConfiguration(origin)
    corsConfig match {
      case _:NoopCorsConfiguration =>
        throw new RuntimeException(s"The CORS configuration ${NoopCorsConfiguration.getClass} is only a placeholder and " +
          s"should not be used. Consider using one of the available CORS Configuration strategies. " +
          s"For more information see the documentation."
        )
      case _ =>
    }
    if (!corsConfig.isOriginValid) {
      throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, s"Origin '${origin.getOrElse("")}' is not authorized")
    }
    function.withCors(corsConfig)
  }

  override protected def handleFailure(exception: Throwable): AwsProxyResponse = {
    val message = s"Internal Server Error: ${exception.message}"
    log.error(message)
    exception match {
      case ex: HandleRequestException =>
        buildError(ex.code, ex.getMessage)

      // General exception, handle it gracefully
      case ex =>

        // report error to SNS Topic
        Try(super.handleFailure(ex))

        // Generate response to send back to the api user
        buildError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: check the logs for more information.")
    }
  }
}
