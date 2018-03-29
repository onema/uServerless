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
package onema.serverlessbase.function


import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset

import com.amazonaws.regions.Regions
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import com.amazonaws.services.sns.AmazonSNSAsync
import com.typesafe.scalalogging.Logger
import onema.core.json.Implicits._
import onema.serverlessbase.configuration.lambda.LambdaConfiguration
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.exception.ThrowableExtensions._
import onema.serverlessbase.model.{ApiGatewayProxyRequest, ErrorMessage}
import org.apache.http.HttpStatus

import scala.io.Source
import scala.util.{Failure, Success, Try}


trait ApiGatewayHandler extends ApiGatewayResponse with LambdaConfiguration {

  //--- Fields ---
  protected val log = Logger("apigateway-handler")

  protected val region: Regions = Regions.fromName(sys.env.getOrElse("AWS_REGION", "us-east-1"))

  private val snsErrorTopic = getValue("/sns/error/topic")

  protected val snsClient: AmazonSNSAsync

  //--- Methods ---
  protected def getRequest(inputStream: InputStream): ApiGatewayProxyRequest = {
    val json = Source.fromInputStream(inputStream).mkString
    log.info(json)
    json.jsonParse[ApiGatewayProxyRequest]
  }

  protected def writeResponse(outputStream: OutputStream, value: AnyRef): Unit = {
    outputStream.write(value.javaClassToJson.getBytes(Charset.defaultCharset()))
    outputStream.close()
  }

  protected def handle(function: () => AwsProxyResponse): AwsProxyResponse = {
    Try(function()) match {
      case Success(response) => response
      case Failure(e: Throwable) => handleFailure(e)
    }
  }

  protected def handleFailure(exception: Throwable): AwsProxyResponse = {
    val message = s"Internal Server Error: ${exception.message}"
    log.error(message)
    exception match {
      case ex: HandleRequestException =>
        ex.httpResponse

      // General exception, handle it gracefully
      case _ =>

        // report error to SNS Topic
        if(snsErrorTopic.isDefined) {
          snsClient.publish(snsErrorTopic.get, ErrorMessage(message).toJson)
        }

        // generate response to send back to the api user
        buildError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error: check the logs for more information.")
    }
  }
}
