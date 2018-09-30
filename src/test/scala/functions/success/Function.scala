/**
  * This file is part of the ONEMA io.onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.success

import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.events.ApiGateway.{AwsProxyRequest, AwsProxyResponse}
import io.onema.userverless.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object Logic {
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    AwsProxyResponse(HttpStatus.SC_OK, body = Some("{\"message\": \"success\"}"))
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Logic.handleRequest(request)
  }
}
