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
import onema.serverlessbase.model.{ApiGatewayProxyRequest, ErrorMessage}
import org.apache.http.HttpStatus

import scala.util.{Failure, Success, Try}


trait ApiGatewayHandler extends ApiGatewayResponse {

  //--- Fields ---
  protected val log = Logger("apigateway-handler")

  protected var lambdaContext: Context = _

  protected lazy val accountId: String = lambdaContext.getInvokedFunctionArn.split(':')(4)

  private val snsErrorTopic = System.getenv("SNS_ERROR_TOPIC")

  private val region = System.getenv("AWS_REGION")

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

  private def handleFailure(exception: Throwable): AwsProxyResponse = {
    log.error(exception.getMessage)
    log.error(exception.getStackTrace.mkString)
    exception match {
      case ex: HandleRequestException =>
        ex.httpResponse

      // General exception, handle it gracefully
      case ex =>
        val errorMessage = s"Internal Server Error: $ex"

        // report error to SNS Topic
        snsClient.publish(snsErrorTopic, ErrorMessage(errorMessage).toJson)

        // generate response to send back to the api user
        buildError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal server errors, check the logs for more information.")
    }
  }
}
