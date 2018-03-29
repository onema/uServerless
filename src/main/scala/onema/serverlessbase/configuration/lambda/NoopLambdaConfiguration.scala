/**
  * This file is part of the ONEMA serverless-base Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.configuration.lambda

trait NoopLambdaConfiguration extends LambdaConfiguration {
  /**
    * Always return None
    *
    * @param path name of the configuration value
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
