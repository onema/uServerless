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

import scala.collection.JavaConverters._

trait ApiGatewayResponse {

  //--- Methods ---
  protected def buildResponse(code: Int, payload: AnyRef): AwsProxyResponse = {
    new AwsProxyResponse(
      code,
      Map("Access-Control-Allow-Origin" -> "*").asJava,
      payload.toJson
    )
  }

  protected def buildError(code: Int, message: String): AwsProxyResponse = {
    buildResponse(code, ErrorMessage(message))
  }
}
