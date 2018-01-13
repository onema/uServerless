/**
  * This file is part of the ONEMA onema Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package functions.handle.error

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import onema.serverlessbase.core.function.ApiGatewayHandler
import onema.serverlessbase.exception.HandleRequestException

object Logic {
  def handleRequest: Nothing = throw new HandleRequestException(400, "FooBar")
}

class Function extends ApiGatewayHandler {
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    handle(() => Logic.handleRequest)
  }
}

