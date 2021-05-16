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

package io.onema.userverless.config.cors

object EnvCorsConfiguration {
  def apply(origin: Option[String]): EnvCorsConfiguration = new EnvCorsConfiguration(origin)
}

class EnvCorsConfiguration(origin: Option[String]) extends CorsConfiguration(origin) {

  //--- Fields ---
  protected val corsSites: Option[String] = sys.env.get("CORS_SITES")

  //--- Methods ---
  /**
    * Check if the environment variable is defined for this function, if is not returns false.
    * @return
    */
  override def isEnabled: Boolean = corsSites.isDefined

  /**
    * Use the method isSiteEnabled to find out if the origin is valid.
    * @return
    */
  override def isOriginValid: Boolean = {
    isSiteEnabled(corsSites)
  }
}
