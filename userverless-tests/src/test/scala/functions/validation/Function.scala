/*
 * This file is part of the ONEMA userverless-tests Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2018-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package functions.validation

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.Logger
import io.onema.userverless.config.NoopLambdaConfiguration
import io.onema.userverless.exception.HandleRequestException
import io.onema.userverless.service.{ApiGatewayHandler, ApiGatewayResponseBuilder}
import org.apache.http.HttpStatus

class ValidationLogic() extends ApiGatewayResponseBuilder {

  //--- Fields ---
  val log = Logger(classOf[ValidationLogic])

  //--- Methods ---
  def validate(request: AwsProxyRequest): Unit = {
    log.info("validateString your message")
    containsOriginHeader(request)
    containsValidJson(request)
  }

  def containsValidJson(request: AwsProxyRequest): Unit = {
    if (Option(request.getBody).isEmpty) {
      throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "The Body of your request is empty")
    }
  }

  def containsOriginHeader(request: AwsProxyRequest): Unit = {
    if(Option(request.getMultiValueHeaders.getFirst("Origin")).isEmpty) {
      throw new HandleRequestException(HttpStatus.SC_UNAUTHORIZED, "The Origin header is required")
    }
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  val logic = new ValidationLogic()

  //--- Validation Setup ---
//  validationListener(logic.validate)

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    log.info("Submit your validated message to a background processing service")
    buildResponse(HttpStatus.SC_ACCEPTED, payload = Message("validation succeeded"))
  }
}

case class Message(message: String)
