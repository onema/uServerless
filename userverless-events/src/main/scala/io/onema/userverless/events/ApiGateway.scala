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

object ApiGateway {

  case class Claims(
    sub: Option[String] = None,
    aud: Option[String] = None,
    email_verified: Option[String] = None,
    event_id: Option[String] = None,
    token_use: Option[String] = None,
    auth_time: Option[String] = None,
    iss: Option[String] = None,
    `cognito:username`: Option[String] = None,
    exp: Option[String] = None,
    iat: Option[String] = None,
    email: Option[String] = None

  )

  case class Authorizer(
    contextProperties: Map[String, String] = Map(),
    claims: Claims = Claims(),
    principalId: Option[String] = None
  )

  case class Identity(
    cognitoIdentityPoolId: Option[String] = None,
    accountId: Option[String] = None,
    cognitoIdentityId: Option[String] = None,
    caller: Option[String] = None,
    sourceIp: Option[String] = None,
    accessKey: Option[String] = None,
    cognitoAuthenticationType: Option[String] = None,
    cognitoAuthenticationProvider: Option[String] = None,
    userArn: Option[String] = None,
    userAgent: Option[String] = None,
    user: Option[String] = None
  )

  case class RequestContext(
    resourceId: Option[String] = None,
    authorizer: Authorizer = Authorizer(),
    resourcePath: Option[String] = None,
    httpMethod: Option[String] = None,
    requestTime: Option[String] = None,
    path: Option[String] = None,
    accountId: Option[String] = None,
    protocol: Option[String] = None,
    stage: Option[String] = None,
    requestTimeEpoch: Long = System.currentTimeMillis / 1000,
    requestId: Option[String] = None,
    identity: Identity = Identity(),
    apiId: Option[String] = None
  )

  case class AwsProxyRequest(
    resource: Option[String] = None,
    path: Option[String] = None,
    httpMethod: Option[String] = None,
    headers: Map[String, String] = Map(),
    queryStringParameters: Option[Map[String, String]] = None,
    pathParameters: Option[Map[String, String]] = None,
    stageVariables: Option[Map[String, String]] = None,
    requestContext: RequestContext = RequestContext(),
    body: Option[String] = None,
    isBase64Encoded: Option[Boolean] = None
  )

  case class AwsProxyResponse(
    statusCode: Int,
    headers: Map[String, String] = Map(),
    body: Option[String] = None,
    base64Encoded: Boolean = false
  )
}
