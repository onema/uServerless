/**
  * This file is part of the ONEMA io.onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.cors

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.userverless.configuration.cors.Extensions.AwsProxyResponseExtension
import io.onema.userverless.configuration.cors.NoopCorsConfiguration
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.ApiGatewayHandler
import org.apache.http.HttpStatus


class NoopFunction extends ApiGatewayHandler with NoopLambdaConfiguration {

  //--- Fields ---
  override protected lazy val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    val origin = request.getHeaders.get("origin")
    new AwsProxyResponse(HttpStatus.SC_OK).withCors(new NoopCorsConfiguration())
  }

}

