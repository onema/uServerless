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

package io.onema.serverlessbase.configuration.cors

import com.amazonaws.services.simplesystemsmanagement.{AWSSimpleSystemsManagementAsync, AWSSimpleSystemsManagementAsyncClientBuilder}
import io.onema.serverlessbase.configuration.lambda.SsmLambdaConfiguration

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

class SsmCorsConfiguration(origin: Option[String], val ssmClient: AWSSimpleSystemsManagementAsync, override val stageName: String)
  extends CorsConfiguration(origin) with SsmLambdaConfiguration {

  //--- Methods ---
  override def isEnabled: Boolean = getValue("/cors/sites").isDefined

  override def isOriginValid: Boolean = {
    val sites = getValue("/cors/sites")
    isSiteEnabled(sites)
  }
}
