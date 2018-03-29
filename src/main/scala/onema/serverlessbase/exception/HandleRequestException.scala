/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.exception

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import onema.serverlessbase.function.ApiGatewayResponse

class HandleRequestException(val code: Int, message: String) extends Exception(message) with ApiGatewayResponse {
  def httpResponse: AwsProxyResponse = {
    buildError(code, getMessage)
  }
}

class RuntimeException(code: Int, message: String) extends HandleRequestException(code, message)
