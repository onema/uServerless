/*
 * This file is part of the ONEMA userverless-core Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2018-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.userverless.config

abstract class CorsConfiguration(val origin: Option[String]) {

  /**
    * Check if CORS is enabled. A check must verify that the config has access to the
    * data store used to keep track of the config.
    * @return
    */
  def isEnabled: Boolean

  /**
    * Check if the origin for this class is valid. The protected method
    * isSiteEnabled is a helper service to help check the contents of the sites.
    * @return
    */
  def isOriginValid: Boolean

  /**
    * Checks if the value for the sites is valid. This can be a single value,
    * coma separated values, '*', or None.
    * @param sites Option containing the sites allowed to call the API
    * @return
    */
  protected def isSiteEnabled(sites: Option[String]): Boolean = {
    if(!isEnabled) {
      false
    } else if(sites.isDefined && sites.getOrElse("") == "*") {
      true
    } else if(sites.isDefined && origin.getOrElse("").nonEmpty) {
      sites.exists(_.split(',').contains(origin.getOrElse("")))
    } else {
      false
    }
  }
}
