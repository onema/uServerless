/*
 * This file is part of the ONEMA userverless-tests Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2019-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package functions.customresource

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectListing
import functions.customresource.S3BucketFunction.{S3Properties, S3Response}
import io.onema.userverless.config.EnvLambdaConfiguration
import io.onema.userverless.events.CloudFormation
import io.onema.userverless.events.CloudFormation.CloudFormationResponse
import io.onema.userverless.service.CustomResourceHandler

import scala.jdk.CollectionConverters._

class S3BucketFunction extends CustomResourceHandler[S3Properties, S3Response] with EnvLambdaConfiguration {
  //--- Properties ---
  val s3 = AmazonS3ClientBuilder.defaultClient()

  //--- Methods ---
  /**
    * Method is invoked when a custom resource is created.
    *
    * @param request request sent by CloudFormation
    * @return CloudFormationResponse
    */
  override def createResource(request: CloudFormation.CloudFormationRequest[S3Properties]): CloudFormation.CloudFormationResponse[S3Response] = {
    val response = request.ResourceProperties match {
      case Some(properties) => s3.createBucket(properties.Name)
      case None => throw new Exception("The resource name is a required parameter")
    }
    val data = S3Response(response.getName, s"arn:aws:s3:::${response.getName}")
    CloudFormationResponse("SUCCESS", request.StackId, request.RequestId, response.getName, Data = Some(data))
  }

  /**
    * Method is invoked when a custom resource is changed/updated.
    *
    * @param request request sent by CloudFormation
    * @return CloudFormationResponse
    */
  override def updateResource(request: CloudFormation.CloudFormationRequest[S3Properties]): CloudFormation.CloudFormationResponse[S3Response] = {
    // ignore updates for this example
    CloudFormationResponse("SUCCESS", request.StackId, request.RequestId, request.PhysicalResourceId.get)
  }

  /**
    * Method is invoked when a custom resource is deleted.
    *
    * @param request request set by CloudFormation
    * @return CloudFormationResponse
    */
  override def deleteResource(request: CloudFormation.CloudFormationRequest[S3Properties]): CloudFormation.CloudFormationResponse[S3Response] = {
    val bucketName = request.ResourceProperties.get.Name
    emptyBucket(bucketName)
    s3.deleteBucket(bucketName)
    CloudFormationResponse("SUCCESS", request.StackId, request.RequestId, request.PhysicalResourceId.get)
  }

  def emptyBucket(bucketName: String) = {
    val list: ObjectListing = s3.listObjects(bucketName)
    list.getObjectSummaries.asScala.foreach(x => s3.deleteObject(bucketName, x.getKey))
  }
}

object S3BucketFunction {
  case class S3Properties(Name: String)
  case class S3Response(Name: String, Arn: String)
}
