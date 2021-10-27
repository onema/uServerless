/*
 * This file is part of the ONEMA userverless-core Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2021-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.userverless.common

import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import io.onema.userverless.config.CorsConfiguration

import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}

object ConfigExtensions {

  implicit class AwsProxyResponseExtension(response: AwsProxyResponse) {

    /**
      * Extension method for the AwsProxyResponse to add the required access control headers to the response
      *
      * @param corsConfiguration the cors config required to validateString the domain
      */
    def withCors(corsConfiguration: CorsConfiguration): AwsProxyResponse = {
      if (corsConfiguration.isOriginValid) {
        corsConfiguration.origin match {
          case Some(origin) =>
            val existingHeaders = Option(response.getHeaders.asScala).getOrElse(Map())
            val headers = Map(
              "Access-Control-Allow-Origin" -> origin,
              "Access-Control-Allow-Credentials" -> "true"
            ) ++ existingHeaders
            response.setHeaders(headers.asJava)
          case None => throw new RuntimeException("Logic error: The origin was mark as valid, yet it's value is empty")
        }
      }
      response
    }
  }
}
