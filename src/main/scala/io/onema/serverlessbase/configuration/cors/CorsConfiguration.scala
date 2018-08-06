/**
  * This file is part of the ONEMA serverless-base Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.serverlessbase.configuration.cors

abstract class CorsConfiguration(val origin: Option[String]) {

  def isEnabled: Boolean

  def isOriginValid: Boolean

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
