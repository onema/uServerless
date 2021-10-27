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

import com.amazonaws.serverless.proxy.model.AwsProxyRequest

object AwsProxyExtensions {
  implicit class AwsProxyRequestExtensions(request: AwsProxyRequest) {
    def origin: Option[String] = Option(request.getMultiValueHeaders.getFirst("origin"))
  }

}
