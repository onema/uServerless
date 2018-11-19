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



object LogMetrics extends Metrics {

  //--- Fields ---
  val log: Logger = Logger(classOf[Metrics])

  //--- Methods ---

  /**
    * Get StatsD friendly tags from the dimensions
    *
    * @return Sting of coma separated k:v pairs
    */
  final lazy val defaultTags: String = dimensions
    .filter { case(_, value) => value.nonEmpty }
    .map { case(k, v) => s"$k:$v" }
    .mkString(",")

  def getTags(customTags: Seq[(String, String)]): String = {
    val result = customTags.map { case(k, v) => s"$k:$v"}.mkString(",")
    if(result.nonEmpty) {
      s"#$defaultTags,$result"
    } else {
      s"#$defaultTags"
    }
  }

  /**
    * Time the execution of a code block
    *
    * @param blockName The name of the code block that will be timed
    * @param codeBlock function that will be timed
    * @tparam T return type of the block
    * @return T
    */
  override def time[T](blockName: String, customTags: (String, String)*)(codeBlock: => T): T = {
    val startTime = System.nanoTime()
    val result = codeBlock
    val endTime = System.nanoTime()
    log.info(s"$blockName:" + (endTime - startTime)/1000 + "|us|@1|" + getTags(customTags))
    result
  }

  /**
    * Record counts, count value should default to one
    * @param metricName name of the counter
    * @param count increment of the counter
    * @param customTags varargs of key value pairs to add to the metric as tags
    */
  override def count(metricName: String, count: Int, customTags: (String, String)*): Unit = {
    log.info(s"$metricName:$count|c|@1|" + getTags(customTags))
  }

  /**
    * Record counts, this is used as a default that increments the counter by one
    * @param metricName name of the counter
    * @param customTags varargs of key value pairs to add to the metric as tags
    */
  override def count(metricName: String, customTags: (String, String)*): Unit = {
    count(metricName, 1, customTags: _*)
  }
}
