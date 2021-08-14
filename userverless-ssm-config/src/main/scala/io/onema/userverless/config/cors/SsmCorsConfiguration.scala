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

import io.onema.userverless.config.lambda.SsmLambdaConfiguration
import software.amazon.awssdk.services.ssm.SsmClient

object SsmCorsConfiguration {
  def apply(origin: Option[String]): SsmCorsConfiguration = {
    SsmCorsConfiguration(origin, SsmClient.builder().build())
  }

  def apply(origin: Option[String], stageName: String): SsmCorsConfiguration = {
    SsmCorsConfiguration(origin, SsmClient.builder().build(), stageName)
  }

  def apply(origin: Option[String], ssmClient: SsmClient): SsmCorsConfiguration = {
    SsmCorsConfiguration(origin, ssmClient, sys.env.getOrElse("STAGE_NAME", ""))
  }

  def apply(origin: Option[String], ssmClient: SsmClient, stageName: String): SsmCorsConfiguration = {
    new SsmCorsConfiguration(origin, ssmClient, stageName)
  }
}

class SsmCorsConfiguration(origin: Option[String], override val ssmClient: SsmClient, override val stageName: String)
  extends CorsConfiguration(origin) with SsmLambdaConfiguration {

  //--- Methods ---
  /**
    * Check if the parameter store exists.
    * @return
    */
  override def isEnabled: Boolean = getValue("/cors/sites").isDefined

  /**
    * Get the value from the parameter store, and check it's contents using the isSiteEnabled method.
    * @return
    */
  override def isOriginValid: Boolean = {
    val sites = getValue("/cors/sites")
    isSiteEnabled(sites)
  }
}
