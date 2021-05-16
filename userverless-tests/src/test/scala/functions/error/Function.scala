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

package functions.error

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.config.cors.EnvCorsConfiguration
import io.onema.userverless.config.cors.Extensions._
import io.onema.userverless.config.lambda.NoopLambdaConfiguration
import io.onema.userverless.extensions.AwsProxyExtensions.AwsProxyRequestExtensions
import io.onema.userverless.function.ApiGatewayHandler

object Logic {
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    throw new NotImplementedError("FooBar")
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Logic
      .handleRequest(request)
      .withCors(new EnvCorsConfiguration(request.origin))
  }
}
