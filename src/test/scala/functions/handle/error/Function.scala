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

package functions.handle.error

import com.amazonaws.serverless.proxy.internal.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import onema.serverlessbase.exception.HandleRequestException
import onema.serverlessbase.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object Logic {
  def handleRequest: Nothing = throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "FooBar")
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {
  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def lambdaHandler(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    handle(() => Logic.handleRequest)
  }
}
