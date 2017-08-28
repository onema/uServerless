package onema.serverlessbase.core.function

import java.util

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import onema.serverlessbase.core.json.Implicits.AnyClassToJsonString
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.model.ErrorMessage
import org.apache.http.HttpStatus

import scala.util.{Failure, Success, Try}

trait ApiGatewayHandler {

  def handle(request: AwsProxyRequest): AwsProxyResponse

  def handleRequest(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Try(handle(request)) match {
      case Success(response) =>
        Console.println("Success Case")
        response
      case Failure(e) => e match {
        case ex: HandleRequestException =>
          Console.println("Handle Error")
          ex.httpResponse
        case _ =>
          Console.println("Internal Server Error")
          new AwsProxyResponse(
            HttpStatus.SC_INTERNAL_SERVER_ERROR,
            new util.HashMap[String, String](),
            ErrorMessage("Internal server errors, check the logs for more information.").toJson
          )
      }
    }
  }
}
