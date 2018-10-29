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
import org.scalatest.{FlatSpec, Matchers}
import io.onema.json.Extensions._
import io.onema.userverless.events.S3.S3Event

import scala.io.Source

class S3Test extends FlatSpec with Matchers {

  "S3 PUT event" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/s3-put-event.json")).mkString

    // Act
    val model = json.jsonDecode[S3Event]

    // Assert
    model.Records.head.eventVersion.get should be("2.0")
    model.Records.head.requestParameters.sourceIPAddress.get should be("127.0.0.1")
    model.Records.head.s3.`object`.key.get should be("HappyFace.jpg")
    model.Records.head.s3.`object`.eTag.get should be("0123456789abcdef0123456789abcdef")
    model.Records.head.s3.bucket.name.get should be("sourcebucket")
    model.Records.head.responseElements.`x-amz-request-id`.get should be("EXAMPLE123456789")
  }

  "S3 DELETE event" should "be converted to json string " in {
    // Arrange
    val json = Source.fromURL(getClass.getResource("/s3-delete-event.json")).mkString

    // Act
    val model = json.jsonDecode[S3Event]

    // Assert
    model.Records.head.eventVersion.get should be("2.0")
    model.Records.head.requestParameters.sourceIPAddress.get should be("127.0.0.1")
    model.Records.head.s3.`object`.key.get should be("HappyFace.jpg")
    model.Records.head.s3.`object`.eTag should be(None)
    model.Records.head.s3.bucket.name.get should be("sourcebucket")
    model.Records.head.responseElements.`x-amz-request-id`.get should be("EXAMPLE123456789")
  }
}
