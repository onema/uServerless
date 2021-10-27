/*
 * This file is part of the ONEMA userverless-core Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2018-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.userverless.monitoring

import cats.effect.{Deferred, IO}

trait Metrics {

  //--- Fields ---
  val dimensions: Map[String, String] = Map(
    "service" -> sys.env.getOrElse("AWS_LAMBDA_FUNCTION_NAME", ""),
    "version" -> sys.env.getOrElse("AWS_LAMBDA_FUNCTION_VERSION", ""),
    "region" -> sys.env.getOrElse("AWS_REGION", ""),
    "stage" -> sys.env.getOrElse("STAGE_NAME", "dev")
  )
  val appName: String = sys.env.getOrElse("APP_NAME", sys.env.getOrElse("AWS_LAMBDA_FUNCTION_NAME", ""))

  //--- Methods ---

  /**
    * Time the execution of a code block
    *
    * @param blockName The name of the code block that will be timed
    * @param codeBlock service that will be timed
    * @tparam T return type of the block
    */
  def time[T](blockName: String, customTags: (String, String)*)(codeBlock: => T): T
  def timeSignal[T](blockName: String, customTags: (String, String)*)(signal: Deferred[IO, Unit]): IO[Unit]
  def timeIO[T](blockName: String, customTags: (String, String)*)(codeBlock: => T): IO[T]

  /**
    * Record counts with a custom value and sample rate.
    * @param metricName name of the counter
    * @param value increment of the counter
    * @param rate custom sample rate, e.g. 0.1 will sample every 1/10 of the time
    * @param customTags varargs of key value pairs to add to the metric as tags
    */
  def count(metricName: String, value: Int, rate: Float, customTags: (String, String)*): Unit
  def countIO(metricName: String, value: Int, rate: Float, customTags: (String, String)*): IO[Unit]

  /**
    * Record counts, this is used as a default that increments the counter by one and does not sample
    * @param metricName name of the counter
    * @param customTags varargs of key value pairs to add to the metric as tags
    */
  def count(metricName: String, customTags: (String, String)*): Unit
  def countIO(metricName: String, customTags: (String, String)*): IO[Unit]

  /**
    *
    * @param gaugeName name of the gauge
    * @param value value of the gauge
    * @param customTags varargs of key value paris to add to the metrics as tags
    */
  def gauge(gaugeName: String, value: Long, customTags: (String, String)*): Unit
  def gaugeIO(gaugeName: String, value: Long, customTags: (String, String)*): IO[Unit]

}

object Metrics {
  case class Dimension(name: String, value: String)
}
