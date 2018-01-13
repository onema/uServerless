package onema.serverlessbase.core.function

import java.util

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.Logger
import org.apache.http.HttpStatus
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import onema.core.json.Implicits._

import scala.util.{Failure, Success, Try}

trait ApiGatewayHandler {

  //--- Fields ---
  val log = Logger("apigateway-handler")

  protected var lambdaContext: Context = _

  protected lazy val accountId: String = lambdaContext.getInvokedFunctionArn.split(':')(4)

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
      case _ =>
        log.info("Internal Server Error")
        new AwsProxyResponse(
          HttpStatus.SC_INTERNAL_SERVER_ERROR,
          new util.HashMap[String, String](),
          ErrorMessage("Internal server errors, check the logs for more information.").toJson
        )
    }
  }
}
