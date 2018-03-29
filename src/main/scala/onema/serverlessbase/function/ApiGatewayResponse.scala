/**
  * This file is part of the ONEMA Default (Template) Project Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.function

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import onema.core.json.Implicits._
import onema.serverlessbase.model.ErrorMessage


trait ApiGatewayResponse {

  //--- Methods ---
  protected def buildResponse(code: Int, payload: AnyRef): AwsProxyResponse = {
    val response = new AwsProxyResponse(code)
    response.setBody(payload.toJson)
    response
  }

  protected def buildError(code: Int, message: String): AwsProxyResponse = {
    buildResponse(code, ErrorMessage(message))
  }
}
