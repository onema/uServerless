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

package io.onema.userverless.configuration.cors

class NoopCorsConfiguration extends CorsConfiguration(None) {

  //--- Methods ---

  /**
    * Is never enabled
    * @return
    */
  override def isEnabled: Boolean = false

  /**
    * No origin is valid
    * @return
    */
  override def isOriginValid: Boolean = false
}

object NoopCorsConfiguration {
  def apply(): NoopCorsConfiguration = new NoopCorsConfiguration()
}
