/**
  * This file is part of the ONEMA Default (Template) Project Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.function

import io.onema.userverless.model.ErrorMessage
import io.onema.json.Extensions._
import io.onema.userverless.events.ApiGateway.AwsProxyResponse


trait ApiGatewayResponse {

  //--- Methods ---
  protected def buildResponse(code: Int, headers: Map[String, String] = Map()): AwsProxyResponse = {
    AwsProxyResponse(code, headers = headers)
  }

  protected def buildResponse(code: Int, payload: AnyRef, headers: Map[String, String]): AwsProxyResponse = {
    AwsProxyResponse(code, headers = headers, body = Some(payload.asJson))
  }

  protected def buildResponse(code: Int, payload: AnyRef): AwsProxyResponse = {
    AwsProxyResponse(code, body = Some(payload.asJson))
  }

  protected def buildError(code: Int, message: String): AwsProxyResponse = {
    buildResponse(code, ErrorMessage(message))
  }
}
