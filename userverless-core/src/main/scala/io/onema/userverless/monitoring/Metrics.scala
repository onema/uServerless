/**
  * This file is part of the ONEMA uServerless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.monitoring

import io.onema.userverless.monitoring.Metrics.Dimension

trait Metrics {

  //--- Fields ---
  var dimensions: Map[String, String] = Map(
    "function" -> sys.env.getOrElse("AWS_LAMBDA_FUNCTION_NAME", ""),
    "version" -> sys.env.getOrElse("AWS_LAMBDA_FUNCTION_VERSION", ""),
    "stage" -> sys.env.getOrElse("STAGE_NAME", "dev")
  )

  //--- Methods ---

  /**
    * Time the execution of a code block
    *
    * @param blockName The name of the code block that will be timed
    * @param codeBlock function that will be timed
    * @tparam T return type of the block
    * @return T
    */
  def time[T](blockName: String)(codeBlock: => T): T

  /**
    * Record counts, count value should default to one
    * @param metricName name of the counter
    * @param count optional value, should default to one
    */
  def count(metricName: String, count: Int): Unit

}

object Metrics {
  case class Dimension(name: String, value: String)
}