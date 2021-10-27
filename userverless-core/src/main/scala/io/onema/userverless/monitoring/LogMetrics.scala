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
import com.typesafe.scalalogging.Logger

import java.time.Instant
import java.time.temporal.ChronoUnit



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

  override def time[T](blockName: String, customTags: (String, String)*)(codeBlock: => T): T = {
    val startTime = Instant.now()
    val result = codeBlock
    reportTime(blockName, customTags, startTime)
    result
  }

  override def timeSignal[T](blockName: String, customTags: (String, String)*)(signal: Deferred[IO, Unit]): IO[Unit] = {
    for {
      startTime <- IO.realTimeInstant
      _ <- signal.get
      _ <- IO { reportTime(blockName, customTags, startTime) }
    } yield ()
  }

  override def timeIO[T](blockName: String, customTags: (String, String)*)(codeBlock: => T): IO[T] = {
    for {
      startTime <- IO.realTimeInstant
      result <- IO { codeBlock }
      _ <- IO { reportTime(blockName, customTags, startTime) }
    } yield result
  }

  override def count(metricName: String, value: Int, rate: Float, customTags: (String, String)*): Unit = {
    log.info(s"$metricName:$value|c|@$rate|" + getTags(customTags))
  }

  override def countIO(metricName: String, value: Int, rate: Float, customTags: (String, String)*): IO[Unit] = IO {
    count(metricName, value, rate, customTags: _*)
  }

  override def count(metricName: String, customTags: (String, String)*): Unit = {
    count(metricName, 1, 1F, customTags: _*)
  }

  override def countIO(metricName: String, customTags: (String, String)*): IO[Unit] = IO {
    count(metricName, customTags: _*)
  }

  private def reportTime[T](blockName: String, customTags: Seq[(String, String)], startTime: Instant): Unit =  {
    val micros: Long = startTime.until(Instant.now(), ChronoUnit.MICROS)
    log.info(s"$blockName:" + micros + "|us|@1|" + getTags(customTags))
  }

  override def gauge(gaugeName: String, value: Long, customTags: (String, String)*): Unit = {
    log.info(s"$gaugeName:$value|g|" + getTags(customTags))
  }

  override def gaugeIO(gaugeName: String, value: Long, customTags: (String, String)*): IO[Unit] = IO {
    gauge(gaugeName, value, customTags: _*)
  }
}
