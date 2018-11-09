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

import com.typesafe.scalalogging.Logger
import net.logstash.logback.argument.StructuredArguments._



object LogMetrics extends Metrics {

  //--- Fields ---
  val log: Logger = Logger(classOf[Metrics])

  //--- Methods ---

  /**
    * Get StatsD friendly tags from the dimensions
    *
    * @return Sting of coma separated k:v pairs
    */
  final lazy val tags: String = dimensions
    .filter(_._2.nonEmpty)
    .map { case(k, v) => s"$k:$v" }
    .mkString(",")

  /**
    * Time the execution of a code block
    *
    * @param blockName The name of the code block that will be timed
    * @param codeBlock function that will be timed
    * @tparam T return type of the block
    * @return T
    */
  override def time[T](blockName: String)(codeBlock: => T): T = {
    val startTime = System.nanoTime()
    val result = codeBlock
    val endTime = System.nanoTime()
    log.info(s"$blockName:" + (endTime - startTime)/1000000 + "|ms|@1|#" + tags, keyValue("metric_name", blockName), keyValue("metric_type", "time"))
    result
  }

  override def count(metricName: String, count: Int = 1): Unit = {
    log.info(s"$metricName:$count|c|@1|$tags", keyValue("metric_name", metricName), keyValue("metric_type", "count"))
  }
}
