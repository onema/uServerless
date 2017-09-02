package onema.serverlessbase.core.function

import java.util

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.Logger
import org.apache.http.HttpStatus
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import onema.serverlessbase.core.json.Implicits._

import scala.util.{Failure, Success, Try}

trait ApiGatewayHandler {

  //--- Fields ---
  val log = Logger("apigateway-handler")

  protected var lambdaContext: Context = _

  protected lazy val accountId: String = lambdaContext.getInvokedFunctionArn.split(':')(4)

  //--- Methods ---
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse

  def handle(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Try(handleRequest(request)) match {
      case Success(response) => response
      case Failure(e: Throwable) => handleFailure(e)
    }
  }

  def handleFailure(exception: Throwable): AwsProxyResponse = {
    log.error(exception.getStackTrace.mkString)
    log.error(exception.getMessage)
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
