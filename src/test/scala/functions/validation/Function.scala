/**
  * This file is part of the ONEMA ServerlessBase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.validation

import io.onema.userverless.events.ApiGateway.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.{ApiGatewayHandler, ApiGatewayResponse}
import io.onema.userverless.function.Extensions.ContextExtension
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

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  private val snsTopicName = System.getenv("SNS_TOPIC")

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val topic = s"arn:aws:sns:$region:${context.accountId}:$snsTopicName"
    val logic = new Logic(snsClient, topic)
    logic.handleRequest(request)
  }
}
