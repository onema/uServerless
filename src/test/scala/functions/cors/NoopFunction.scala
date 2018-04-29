/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package functions.cors

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import onema.serverlessbase.configuration.cors.Extensions.AwsProxyResponseExtension
import onema.serverlessbase.configuration.cors.NoopCorsConfiguration
import onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import onema.serverlessbase.function.ApiGatewayHandler
import org.apache.http.HttpStatus


class NoopFunction extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val origin = request.getHeaders.get("origin")
    handle{
      new AwsProxyResponse(HttpStatus.SC_OK)
    }.withCors(new NoopCorsConfiguration())
  }

}

