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

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.sns.AmazonSNSAsync
import com.typesafe.scalalogging.Logger
import io.onema.json.JavaExtensions._
import io.onema.serverlessbase.configuration.lambda.LambdaConfiguration
import io.onema.serverlessbase.exception.HandleRequestException
import io.onema.serverlessbase.exception.ThrowableExtensions._
import org.apache.http.HttpStatus

import scala.io.Source
import scala.util.Try


trait ApiGatewayHandler extends LambdaHandler[AwsProxyRequest, AwsProxyResponse]
  with ApiGatewayResponse
  with LambdaConfiguration {

  //--- Fields ---
  override protected val log = Logger("apigateway-handler")

  protected val snsClient: AmazonSNSAsync

  //--- Methods ---
  protected def getRequest(inputStream: InputStream): AwsProxyRequest = {
    val json = Source.fromInputStream(inputStream).mkString
    val request = json.jsonDecode[AwsProxyRequest]
    log.info(request.asJson)
    request
  }

  protected def writeResponse(outputStream: OutputStream, value: AnyRef): Unit = {
    outputStream.write(value.asJson.getBytes(Charset.defaultCharset()))
    outputStream.close()
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
