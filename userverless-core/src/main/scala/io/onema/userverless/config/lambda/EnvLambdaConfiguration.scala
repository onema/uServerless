/**
  * This file is part of the ONEMA onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.config.lambda

import scala.jdk.CollectionConverters.MapHasAsScala


trait EnvLambdaConfiguration extends Configuration {

  //--- Fields ---
  protected val stageName: String = sys.env.getOrElse("STAGE_NAME", "")

  //--- Methods ---
  /**
    * Get environment variables values by name. This assumes that the name uses a path notation
    * e.g. the path "/database/username" would look for the DATABASE_USERNAME environment variable.
    *
    * @param path name of the parameter
    * @return
    */
  def getValue(path: String): Option[String] = {
    val name = path.replace('/', '_').replaceAll("^_", "").toUpperCase()
    sys.env.get(name)
  }

  /**
    * Get multiple values using a prefix.
    * e.g. the path "/database" would return /database/username, /database/password, /database/port, /database/host, etc.
    *
    * @param path name of the path to search for
    * @return
    */
  def getValues(path: String): Map[String, String] = {
    val prefix = path.replace('/', '_').toUpperCase()
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]].asScala
    val keys = map.keys.filter(x => x.startsWith(prefix))
    keys.map(x => x.replace('_', '/').toLowerCase() -> map(x)).toMap
  }
}
