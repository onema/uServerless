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

package functions.validation

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClient}
import onema.serverlessbase.core.function.ApiGatewayHandler

class Logic(val snsClient: AmazonSNS, val topic: String) {

  //--- Methods ---
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    snsClient.publish(
      new PublishRequest(topic, "Message from Success Function")
    )
    val response = new AwsProxyResponse(200)
    response.setBody("{\"message\": \"validation succeeded\"}")
    response
  }
}

class Function extends ApiGatewayHandler {

  //--- Fields ---
  private val snsTopicName = System.getenv("SNS_TOPIC")

  private val region = System.getenv("AWS_REGION")

  val snsClient = new AmazonSNSClient()

  //--- Methods ---
  protected def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val topic = s"arn:aws:sns:$region:$accountId:$snsTopicName"
    val logic = new Logic(snsClient, topic)
    handle(() => logic.handleRequest(request))
  }
}
