package onema.serverlessbase.core.function

import java.util

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import onema.serverlessbase.core.json.Implicits.AnyClassToJsonString
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import org.apache.http.HttpStatus
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

trait ApiGatewayHandler {

  // --- Fields ---
  val log = Logger("serverless")

  def handle(request: AwsProxyRequest): AwsProxyResponse

  def handleRequest(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Try(handle(request)) match {
      case Success(response) =>
        log.info(s"Success Case")
        response
      case Failure(e) => e match {
        case ex: HandleRequestException =>
          log.info("Handle Error")
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
}
