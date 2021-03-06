/**
  * This file is part of the ONEMA io.onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.ssmparameters

import io.onema.userverless.config.lambda.SsmLambdaConfiguration


object Function extends SsmLambdaConfiguration {

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
