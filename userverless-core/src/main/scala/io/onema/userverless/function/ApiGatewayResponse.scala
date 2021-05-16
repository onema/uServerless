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

import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import io.onema.json.Extensions._
import io.onema.userverless.model.ApiGatewayErrorMessage

import scala.collection.JavaConverters._


trait ApiGatewayResponse {

  //--- Methods ---

  /**
    * Helper methods to build the AWS Proxy Response
    *
    * @param code HTTP Code
    * @param payload The payload that will be in the body of the response
    * @param headers Map of headers
    * @return AwsProxyResponse
    */
  protected def buildResponse(code: Int, payload: AnyRef = null, headers: Map[String, String] = Map()): AwsProxyResponse = {
    val response = new AwsProxyResponse(code)
    response.setHeaders(headers.asJava)
    Option(payload).map(_.asJson).foreach(response.setBody)
    response
  }

  /**
    * Helper method to build an error response
    * @param code HTTP Code
    * @param message The error message
    * @return AwsProxyResponse
    */
  protected def buildError(code: Int, message: String, cause: String = ""): AwsProxyResponse = {
    buildResponse(code, payload = ApiGatewayErrorMessage(message, cause))
  }
}
