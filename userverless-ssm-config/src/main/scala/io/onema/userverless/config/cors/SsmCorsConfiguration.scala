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

import com.amazonaws.services.simplesystemsmanagement.{AWSSimpleSystemsManagementAsync, AWSSimpleSystemsManagementAsyncClientBuilder}
import io.onema.userverless.config.lambda.SsmLambdaConfiguration

object SsmCorsConfiguration {
  def apply(origin: Option[String]): SsmCorsConfiguration = {
    SsmCorsConfiguration(origin, AWSSimpleSystemsManagementAsyncClientBuilder.defaultClient())
  }

  def apply(origin: Option[String], stageName: String): SsmCorsConfiguration = {
    SsmCorsConfiguration(origin, AWSSimpleSystemsManagementAsyncClientBuilder.defaultClient(), stageName)
  }

  def apply(origin: Option[String], ssmClient: AWSSimpleSystemsManagementAsync): SsmCorsConfiguration = {
    SsmCorsConfiguration(origin, ssmClient, sys.env.getOrElse("STAGE_NAME", ""))
  }

  def apply(origin: Option[String], ssmClient: AWSSimpleSystemsManagementAsync, stageName: String): SsmCorsConfiguration = {
    new SsmCorsConfiguration(origin, ssmClient, stageName)
  }
}

class SsmCorsConfiguration(origin: Option[String], override val ssmClient: AWSSimpleSystemsManagementAsync, override val stageName: String)
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
