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

package onema.serverlessbase.configuration.cors

class EnvCorsConfiguration(origin: Option[String]) extends CorsConfiguration(origin) {

  //--- Fields ---
  protected val corsSites: Option[String] = sys.env.get("CORS_SITES")

  //--- Methods ---
  override def isEnabled: Boolean = corsSites.isDefined

  override def isOriginValid: Boolean = {
    isSiteEnabled(corsSites)
  }
}
