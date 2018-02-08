package onema.serverlessbase.function


import java.io.{InputStream, OutputStream}
import java.nio.charset.Charset
import java.util.Scanner

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.typesafe.scalalogging.Logger
import onema.core.json.Implicits._
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.exception.ThrowableExtensions._
import onema.serverlessbase.model.{ApiGatewayProxyRequest, ErrorMessage}
import org.apache.http.HttpStatus

import scala.util.{Failure, Success, Try}


trait ApiGatewayHandler extends ApiGatewayResponse {

  //--- Fields ---
  protected val log = Logger("apigateway-handler")

  protected var lambdaContext: Context = _

  protected lazy val accountId: String = lambdaContext.getInvokedFunctionArn.split(':')(4)

  private val snsErrorTopic = sys.env.get("SNS_ERROR_TOPIC")

  private val region = sys.env.get("AWS_REGION")

  private val snsClient = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  protected def getRequest(inputStream: InputStream): ApiGatewayProxyRequest = {
    val scanner = new Scanner(inputStream, "utf-8")
    val json: String = scanner.useDelimiter("\\A").next()
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
      case ex =>

        // report error to SNS Topic
        if(snsErrorTopic.isDefined) {
          snsClient.publish(snsErrorTopic.get, ErrorMessage(message).toJson)
        }

        // generate response to send back to the api user
        buildError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal server errors, check the logs for more information.")
    }
  }
}
