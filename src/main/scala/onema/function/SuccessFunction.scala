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

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import onema.serverlessbase.core.function.ApiGatewayHandler

class SuccessFunction extends ApiGatewayHandler {

  //--- Methods ---
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    val response = new AwsProxyResponse(200)
    response.setBody("{\"message\": \"success\"}")
    response
  }
}
