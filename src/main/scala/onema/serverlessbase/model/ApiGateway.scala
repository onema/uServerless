/**
  * This file is part of the ONEMA lambda-sample-request Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.model

case class ApiGatewayProxyRequest(
  resource: String = "",
  path: String = "",
  httpMethod: String = "",
  headers: Headers = Headers(),
  queryStringParameters: String = "",
  pathParameters: String = "",
  stageVariables: String = "",
  requestContext: RequestContext = RequestContext(),
  body: String = "",
  isBase64Encoded: Boolean = false
)

case class Headers(
  Accept: String = "",
  `Accept-Encoding`: String = "",
  `Accept-Language`: String = "",
  Authorization: String = "",
  `CloudFront-Forwarded-Proto`: String = "",
  `CloudFront-Is-Desktop-Viewer`: String = "",
  `CloudFront-Is-Mobile-Viewer`: String = "",
  `CloudFront-Is-SmartTV-Viewer`: String = "",
  `CloudFront-Is-Tablet-Viewer`: String = "",
  `CloudFront-Viewer-Country`: String = "",
  `content-type`: String = "",
  Host: String = "",
  origin: String = "",
  Referer: String = "",
  `User-Agent`: String = "",
  Via: String = "",
  `X-Amz-Cf-Id`: String = "",
  `X-Amzn-Trace-Id`: String = "",
  `X-Forwarded-For`: String = "",
  `X-Forwarded-Port`: String = "",
  `X-Forwarded-Proto`: String = ""
)

case class Claims(
  sub: String = "",
  aud: String = "",
  email_verified: String = "",
  event_id: String = "",
  token_use: String = "",
  auth_time: String = "",
  iss: String = "",
  `cognito:username`: String = "",
  exp: String = "",
  iat: String = "",
  email: String = ""
)

case class Authorizer(
  claims: Claims = Claims()
)

case class Identity(
  cognitoIdentityPoolId: String = "",
  accountId: String = "",
  cognitoIdentityId: String = "",
  caller: String = "",
  sourceIp: String = "",
  accessKey: String = "",
  cognitoAuthenticationType: String = "",
  cognitoAuthenticationProvider: String = "",
  userArn: String = "",
  userAgent: String = "",
  user: String = ""
)

case class RequestContext(
  resourceId: String = "",
  authorizer: Authorizer = Authorizer(),
  resourcePath: String = "",
  httpMethod: String = "",
  requestTime: String = "",
  path: String = "",
  accountId: String = "",
  protocol: String = "",
  stage: String = "",
  requestTimeEpoch: Double = System.currentTimeMillis / 1000,
  requestId: String = "",
  identity: Identity = Identity(),
  apiId: String = ""
)

case class ApiGatewayProxyResponse(
  statusCode: Int,
  headers: Map[String, String] = Map(),
  body: String = "",
  isBase64Encoded: Boolean = false
)

