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

package onema.function

import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest
import onema.serverlessbase.core.function.ApiGatewayHandler

class ErrorFunction extends ApiGatewayHandler {
  override def handleRequest(request: AwsProxyRequest): Nothing = {
    throw new NotImplementedError("FooBar")
  }
}
