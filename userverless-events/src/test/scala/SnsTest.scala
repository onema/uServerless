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
import io.onema.userverless.events.Sns.SnsEvent
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class SnsTest extends FlatSpec with Matchers {

  "Sns topic notification" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/sns-topic-notification.json")).mkString

    // Act
    val model = json.jsonDecode[SnsEvent]

    // Assert
    model.Records.head.Sns.Type should be("Notification")
    model.Records.head.Sns.Timestamp.get should be("1970-01-01T00:00:00.000Z")
    model.Records.head.Sns.MessageAttributes.get("TestBinary").Type should be("Binary")
  }

  "Sns custom topic notification" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/sns-topic-notification-custom.json")).mkString

    // Act
    val model = json.jsonDecode[SnsEvent]

    // Assert
    model.Records.head.Sns.Type should be("Notification")
    model.Records.head.Sns.MessageAttributes.get.get("TestBinary") should be(None)
    model.Records.head.Sns.Subject should be(None)
    model.Records.head.Sns.Message should not be None
  }
}
