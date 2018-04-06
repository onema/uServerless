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

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync
import onema.serverlessbase.configuration.lambda.SsmLambdaConfiguration

class SsmCorsConfiguration(origin: Option[String], val ssmClient: AWSSimpleSystemsManagementAsync) extends CorsConfiguration(origin) with SsmLambdaConfiguration {

  //--- Methods ---
  override def isEnabled: Boolean = getValue("/cors/sites").isDefined

  override def isOriginValid: Boolean = {
    val sites = getValue("/cors/sites")
    isSiteEnabled(sites)
  }
}
