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

package io.onema.userverless.events

object CloudTrailLambdaEvent {
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

  case class RequestParameters(
    functionName: Option[String],
    kMSKeyArn: Option[String],
    role: Option[String],
    memorySize: Option[Double],
    code: Option[Code],
    timeout: Option[Double],
    environment: Option[Code],
    deadLetterConfig: Option[Code],
    tracingConfig: Option[TracingConfig],
    publish: Option[Boolean],
    description: Option[String],
    handler: Option[String],
    runtime: Option[String]
  )

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
    responseElements: Option[ResponseElements],
    requestID: Option[String],
    eventID: Option[String],
    eventType: Option[String],
    apiVersion: Option[String]
  )

  case class Resources()
  case class Code()
  case class TracingConfig(mode: String)
  case class Environment()
  case class ResponseElements(
    role: Option[String],
    revisionId: Option[String],
    handler: Option[String],
    memorySize: Option[Double],
    runtime: Option[String],
    functionArn: Option[String],
    functionName: Option[String],
    codeSize: Option[Double],
    version: Option[String],
    tracingConfig: Option[TracingConfig],
    description: Option[String],
    lastModified: Option[String],
    codeSha256: Option[String],
    environment: Option[Environment],
    timeout: Option[Double]
  )

  case class LambdaEvent(
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
