/**
  * This file is part of the ONEMA ServerlessBase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.validation

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.Logger
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.exception.HandleRequestException
import io.onema.userverless.function.{ApiGatewayHandler, ApiGatewayResponse}
import org.apache.http.HttpStatus

class ValidationLogic() extends ApiGatewayResponse {

  //--- Fields ---
  val log = Logger(classOf[ValidationLogic])

  //--- Methods ---
  def validate(request: AwsProxyRequest): Unit = {
    log.info("validate your message")
    containsOriginHeader(request)
    containsValidJson(request)
  }

  def containsValidJson(request: AwsProxyRequest): Unit = {
    if (Option(request.getBody).isEmpty) {
      throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "The Body of your request is empty")
    }
  }

  def containsOriginHeader(request: AwsProxyRequest): Unit = {
    if(Option(request.getHeaders.get("Origin")).isEmpty) {
      throw new HandleRequestException(HttpStatus.SC_UNAUTHORIZED, "The Origin header is required")
    }
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  val logic = new ValidationLogic()

  //--- Validation Setup ---
  validationListener(logic.validate)

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    log.info("Submit your validated message to a background processing function")
    buildResponse(HttpStatus.SC_ACCEPTED, Message("validation succeeded"))
  }
}

case class Message(message: String)
