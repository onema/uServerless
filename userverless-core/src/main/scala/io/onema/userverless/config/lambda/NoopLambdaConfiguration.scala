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

package io.onema.userverless.config.lambda

trait NoopLambdaConfiguration extends Configuration {
  /**
    * Always return None
    *
    * @param path name of the config value
    * @return
    */
  def getValue(path: String): Option[String] = None

  /**
    * Always get empty map
    *
    * @param path name of the path to search for
    * @return
    */
  def getValues(path: String): Map[String, String] = Map()
}
