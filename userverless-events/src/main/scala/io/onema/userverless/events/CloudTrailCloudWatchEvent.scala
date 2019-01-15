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

package io.onema.userverless.events

object CloudTrailCloudWatchEvent {
  case class Attributes(mfaAuthenticated: String, creationDate: String)

  case class SessionContext(attributes: Attributes)

  case class UserIdentity(
    `type`: Option[String],
    principalId: Option[String],
    arn: Option[String],
    accountId: Option[String],
    accessKeyId: Option[String],
    userName: Option[String],
    sessionContext: Option[SessionContext],
    invokedBy: Option[String]
  )

  case class RequestParameters(logGroupName: String)

  case class Detail(
    eventVersion: Option[String],
    userIdentity: Option[UserIdentity],
    eventTime: Option[String],
    eventSource: Option[String],
    eventName: Option[String],
    awsRegion: Option[String],
    errorCode: Option[String],
    errorMessage: Option[String],
    sourceIPAddress: Option[String],
    userAgent: Option[String],
    requestParameters: Option[RequestParameters],
    responseElements: Option[String],
    requestID: Option[String],
    eventID: Option[String],
    eventType: Option[String],
    apiVersion: Option[String]
  )

  case class Resources()

  case class CloudWatchLogEvent(
    version: Option[String],
    id: Option[String],
    detailType: Option[String],
    source: Option[String],
    account: Option[String],
    time: Option[String],
    region: Option[String],
    resources: Option[List[Resources]],
    detail: Detail
  )
}