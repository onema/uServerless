/**
  * This file is part of the ONEMA ServerlessBase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.function

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import onema.serverlessbase.core.function.ApiGatewayHandler

class ValidationFunction extends ApiGatewayHandler {

  //--- Fields ---
  private val snsTopicName = System.getenv("SNS_TOPIC")
  private val region = System.getenv("AWS_REGION")
  private val accountId = System.getenv("ACCOUNT_ID")
  val snsClient = new AmazonSNSClient()

  //--- Methods ---
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    snsClient.publish(
      new PublishRequest(s"arn:aws:sns:$region:$accountId:$snsTopicName", "Message from Success Function")
    )
    val response = new AwsProxyResponse(200)
    response.setBody("{\"message\": \"validation succeeded\"}")
    response
  }
}
