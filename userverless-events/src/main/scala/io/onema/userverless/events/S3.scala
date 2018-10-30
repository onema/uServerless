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

object S3 {
  case class UserIdentity(principalId: Option[String])

  case class RequestParameters(sourceIPAddress: Option[String])

  case class ResponseElements(
    `x-amz-request-id`: Option[String],
    `x-amz-id-2`: Option[String]
  )

  case class Bucket(
    name: Option[String],
    ownerIdentity: Option[UserIdentity],
    arn: Option[String]
  )

  case class ObjectBis(
    key: Option[String],
    size: Option[Double],
    eTag: Option[String],
    sequencer: Option[String]
  )

  case class S3(
    s3SchemaVersion: Option[String],
    configurationId: Option[String],
    bucket: Bucket,
    `object`: ObjectBis
  )

  case class S3EventNotificationRecord(
    eventVersion: Option[String],
    eventSource: Option[String],
    awsRegion: Option[String],
    eventTime: Option[String],
    eventName: Option[String],
    userIdentity: UserIdentity,
    requestParameters: RequestParameters,
    responseElements: ResponseElements,
    s3: S3
  )

  case class S3Event(Records: List[S3EventNotificationRecord])
}
