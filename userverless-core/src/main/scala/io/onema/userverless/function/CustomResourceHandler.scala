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

package io.onema.userverless.function
import java.io.OutputStreamWriter
import java.net.{HttpURLConnection, URL}

import com.amazonaws.services.lambda.runtime.Context
import io.onema.json.Extensions._
import io.onema.userverless.events.CloudFormation.{CloudFormationRequest, CloudFormationResponse}
import io.onema.userverless.events.Sns.SnsEvent
import io.onema.userverless.exception.MessageDecodingException
import io.onema.userverless.extensions.ThrowableExtensions._

import scala.util.{Failure, Success, Try}

abstract class CustomResourceHandler[TRequestProperties: Manifest, TResponseData: Manifest] extends LambdaHandler[CloudFormationRequest[TRequestProperties], Unit]{

  //--- Methods ---
  /**
    * Method is invoked when a custom resource is created.
    * @param request request sent by CloudFormation
    * @return CloudFormationResponse
    */
  def createResource(request: CloudFormationRequest[TRequestProperties]): CloudFormationResponse[TResponseData]

  /**
    * Method is invoked when a custom resource is changed/updated.
    * @param request request sent by CloudFormation
    * @return CloudFormationResponse
    */
  def updateResource(request: CloudFormationRequest[TRequestProperties]): CloudFormationResponse[TResponseData]

  /**
    * Method is invoked when a custom resource is deleted.
    * @param request request set by CloudFormation
    * @return CloudFormationResponse
    */
  def deleteResource(request: CloudFormationRequest[TRequestProperties]): CloudFormationResponse[TResponseData]

  /**
    * This method should be implemented by all lambda functions a and is called by the lambda handler. Please note that
    * the main entry to the lambda codeBlock should be the "lambdaHandler".
    *
    * @param event   TEvent
    * @param context AWS Context
    * @return TResponse, if not response is required set this to Unit
    */
  override def execute(event: CloudFormationRequest[TRequestProperties], context: Context): Unit = {

    val result = event.RequestType.toLowerCase match {
      case "create" => Try(createResource(event))
      case "update" => Try(updateResource(event))
      case "delete" => Try(deleteResource(event))
      case _ => throw new RuntimeException(s"The CloudFormation request type ${event.RequestType} is not valid, supported values are Create, Update, Delete.")
    }
    val payload: String = result match {
      case Success(response) => response.asJson
      case Failure(ex) => CloudFormationResponse("FAILED", event.StackId, event.RequestId, event.LogicalResourceId, Some(ex.structuredMessage(false))).asJson
    }
    put(payload, event.ResponseURL) match {
      case 200 => log.info("Response successfully uploaded")
      case value => log.warn(s"Response payload on put URL returned a $value status code!")
    }
  }

  /**
    * Decode for custom resource. First check if the event is an SNS Event, if not decode the CloudFormationRequest
    * @param json string event
    * @return TEvent
    */
  override def jsonDecode(json: String): CloudFormationRequest[TRequestProperties] = {
    Try(json.jsonDecode[SnsEvent]) match {
      case Success(snsEvent: SnsEvent) => decodeFromSnsEvent(snsEvent)
      case Failure(_) => json.jsonDecode[CloudFormationRequest[TRequestProperties]]
    }
  }

  /**
    * custom decoder if the request has been wrapped in a SNS Event
    * @param snsEvent sns event to be unpacked
    * @return CloudFormationRequest
    */
  def decodeFromSnsEvent(snsEvent: SnsEvent): CloudFormationRequest[TRequestProperties] = snsEvent.Records match {
    case record::Nil =>
      record.Sns.Message.map(_.jsonDecode[CloudFormationRequest[TRequestProperties]]) match {
        case Some(event: CloudFormationRequest[TRequestProperties]) => event
        case None => throw new MessageDecodingException("No message class available for decoding")
      }
    case _ =>
      throw new MessageDecodingException("The SNS Event contains no records, this should never happen!")

  }

  /**
    * Put the response payload to the preSignedUrl
    * @param jsonPayload the payload that will be sent to the pre-signed url
    * @param preSignedUrl the url to submit the payload to
    * @return status code
    */
  private def put(jsonPayload: String, preSignedUrl: String): Int = {
    val connection = new URL(preSignedUrl).openConnection().asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setRequestMethod("PUT")
    val out = new OutputStreamWriter(connection.getOutputStream)
    out.write(jsonPayload)
    out.close()
    connection.getResponseCode
  }
}
