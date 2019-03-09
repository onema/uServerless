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

object CloudFormation {
  case class CloudFormationRequest[TProperties](
    RequestType: String,
    ResponseURL: String,
    StackId: String,
    RequestId: String,
    ResourceType: String,
    LogicalResourceId: String,
    PhysicalResourceId: Option[String] = None,
    ResourceProperties: Option[TProperties] = None,
    OldResourceProperties: Option[TProperties] = None
  )

  case class CloudFormationResponse[TData](
    Status: String,
    StackId: String,
    RequestId: String,
    LogicalResourceId: String,
    Reason: Option[String] = None,
    PhysicalResourceId: Option[String] = None,
    NoEcho: Option[Boolean] = None,
    Data: Option[TData] = None
  )
}
