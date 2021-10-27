/*
 * This file is part of the ONEMA userverless-core Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2021-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.userverless.config

trait MemoryLambdaConfiguration extends Configuration {

  //--- Fields ---
  protected def map: Map[String, String]

  //--- Methods ---

  /**
    * Get environment variables values by name. This assumes that the name uses a path notation
    * e.g. the path "/database/username" would look for the DATABASE_USERNAME environment variable.
    *
    * @param path name of the parameter
    * @return
    */
  def getValue(path: String): Option[String] = {
    if (map.contains(path)) {
      Some(map(path))
    } else {
      None
    }
  }

  /**
    * Get multiple values using a prefix.
    * e.g. the path "/database" would return /database/username, /database/password, /database/port, /database/host, etc.
    *
    * @param path name of the path to search for
    * @return
    */
  def getValues(path: String): Map[String, String] = map.filter { case (key, _) => key.startsWith(path) }
}
