/**
  * This file is part of the ONEMA io.onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.error

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.serverlessbase.configuration.cors.EnvCorsConfiguration
import io.onema.serverlessbase.configuration.cors.Extensions._
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import io.onema.serverlessbase.function.ApiGatewayHandler

object Logic {
  def handleRequest(request: AwsProxyRequest): Nothing = {
    throw new NotImplementedError("FooBar")
  }
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    handle {
      Logic.handleRequest(request)
    }.withCors(new EnvCorsConfiguration(Option(request.getHeaders.get("origin"))))
  }
}
