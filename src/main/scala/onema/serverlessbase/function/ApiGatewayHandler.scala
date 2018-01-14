package onema.serverlessbase.function

import java.util

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSClient
import com.typesafe.scalalogging.Logger
import onema.core.json.Implicits._
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import org.apache.http.HttpStatus

import scala.util.{Failure, Success, Try}

trait ApiGatewayHandler {

  //--- Fields ---
  protected val log = Logger("apigateway-handler")

  protected lazy val accountId: String = lambdaContext.getInvokedFunctionArn.split(':')(4)

  private val snsErrorTopic = System.getenv("SNS_ERROR_TOPIC")

  private val region = System.getenv("AWS_REGION")

  private val snsClient = new AmazonSNSClient()

  protected var lambdaContext: Context = _

  //--- Methods ---
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
        new AwsProxyResponse(
          HttpStatus.SC_INTERNAL_SERVER_ERROR,
          new util.HashMap[String, String](),
          ErrorMessage("Internal server errors, check the logs for more information.").toJson
        )
    }
  }
}
