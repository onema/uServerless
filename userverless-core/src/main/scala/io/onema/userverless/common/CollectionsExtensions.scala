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

import com.amazonaws.serverless.proxy.model.Headers

object CollectionsExtensions {
  implicit class MapExtensions(headersMap: Map[String, String]) {
    def asHeaders: Headers = {
      val headers = new Headers()
      for ((key, value) <- headersMap) {
        headers.add(key, value)
      }
      headers
    }
  }
}
