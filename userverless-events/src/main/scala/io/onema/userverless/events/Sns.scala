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

package io.onema.userverless.events


object Sns {

  case class MessageAttribute(
    Type: String,
    Value: String
  )

  case class SnsRecord(
    Type: String = "",
    MessageId: String = "",
    TopicArn: String = "",
    Subject: Option[String] = None,
    Message: Option[String] = None,
    Timestamp: Option[String] = None,
    SignatureVersion: Option[String] = None,
    Signature: Option[String] = None,
    SigningCertUrl: Option[String] = None,
    UnsubscribeUrl: Option[String] = None,
    MessageAttributes: Option[Map[String, MessageAttribute]] = None
  )

  case class SnsRecords(
    EventSource: Option[String] = None,
    EventVersion: Option[String] = None,
    EventSubscriptionArn: String = "",
    Sns: SnsRecord = SnsRecord()
  )

  case class SnsEvent(
    Records: List[SnsRecords]
  )
}
