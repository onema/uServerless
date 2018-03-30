/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package functions.ssmparameters

import com.amazonaws.services.simplesystemsmanagement.{AWSSimpleSystemsManagementAsync, AWSSimpleSystemsManagementAsyncClientBuilder}
import onema.serverlessbase.configuration.lambda.SsmLambdaConfiguration


object Function extends SsmLambdaConfiguration {

  //--- Fields ---
  override protected val ssmClient: AWSSimpleSystemsManagementAsync = AWSSimpleSystemsManagementAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def main(args: Array[String]): Unit = {
    lambdaHandler()
  }

  def lambdaHandler(): String = {
    val foo = getValue("/foo//foo")
    val blah = getValue("/cors/enabled")
    val barPath = getValues("/bar")
    s"$foo,$barPath"
  }
}