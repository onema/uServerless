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

package io.onema.serverlessbase.configuration.cors

class NoopCorsConfiguration extends CorsConfiguration(None) {

  //--- Methods ---
  override def isEnabled: Boolean = false

  override def isOriginValid: Boolean = false
}
