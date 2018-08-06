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

package io.onema.serverlessbase.configuration.lambda

trait LambdaConfiguration {
  /**
    * Get configuration values by name. This assumes that the name uses a path notation
    * @param path name of the configuration value
    * @return
    */
  def getValue(path: String): Option[String]

  /**
    * Get multiple values using a prefix.
    * e.g. the path "/database" would return /database/username, /database/password, /database/port, /database/host, etc.
    * @param path name of the path to search for
    * @return
    */
  def getValues(path: String): Map[String, String]
}
