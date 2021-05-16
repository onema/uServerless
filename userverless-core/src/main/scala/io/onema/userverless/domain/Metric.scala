/**
  * This file is part of the ONEMA uServerless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2019, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.domain

case class Metric(name: String, value: Double, metricType: String, sampleRate: String, tagMap: Map[String, String], appName: String) {
  def unit: String = metricType match {
    case "c" => "Count"
    case "s" => "Seconds"
    case "ms" => "Milliseconds"
    case "us" => "Microseconds"
    case notSupportedMetric: String => throw new RuntimeException(s"The metric '$notSupportedMetric' is not supported")
  }

  override def toString: String = {
    val tags = tagMap.map {case(k, v) => s"$k:$v"}.mkString(",")
    s"$name:$value|$metricType|$sampleRate|#$tags"
  }
}
