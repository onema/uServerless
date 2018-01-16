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
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSAsyncClientBuilder}
import onema.serverlessbase.function.{ApiGatewayHandler, ApiGatewayResponse}
import org.apache.http.HttpStatus

class Logic(val snsClient: AmazonSNS, val topic: String) extends ApiGatewayResponse {

  //--- Methods ---
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    snsClient.publish(
      new PublishRequest(topic, "Message from Success Function")
    )
    buildResponse(HttpStatus.SC_OK, Map("message" -> "validation succeeded"))
  }
}

class Function extends ApiGatewayHandler {

  //--- Fields ---
  private val snsTopicName = System.getenv("SNS_TOPIC")

  private val region = System.getenv("AWS_REGION")

  private val snsClient = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  protected def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val topic = s"arn:aws:sns:$region:$accountId:$snsTopicName"
    val logic = new Logic(snsClient, topic)
    handle(() => logic.handleRequest(request))
  }
}
