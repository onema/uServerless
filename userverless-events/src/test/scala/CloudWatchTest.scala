/**
  * This file is part of the ONEMA uServerlessEvents Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

import io.onema.json.Extensions._
import io.onema.userverless.events.CloudWatch.ScheduledEvent
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class CloudWatchTest extends FlatSpec with Matchers {

  "CloudWatch Schedule event" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/cloud-watch-scheduled-event.json")).mkString

    // Act
    val model = json.jsonDecode[ScheduledEvent]

    // Assert
    model.`detail-type` should be("Scheduled Event")
    model.time should be("1970-01-01T00:00:00Z")
    model.version.isDefined should be(true)
    model.version.get should be("0")
  }

  "CloudWatch Schedule event without version" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/cloud-watch-scheduled-event-wo-version.json")).mkString

    // Act
    val model = json.jsonDecode[ScheduledEvent]

    // Assert
    model.`detail-type` should be("Scheduled Event")
    model.time should be("1970-01-01T00:00:00Z")
    model.version.isEmpty should be(true)
    model.version should be(None)
  }
}
