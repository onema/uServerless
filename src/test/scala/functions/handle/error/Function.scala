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

package functions.handle.error

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.exception.HandleRequestException
import io.onema.userverless.function.ApiGatewayHandler
import org.apache.http.HttpStatus

object Logic {
  def handleRequest: Nothing = throw new HandleRequestException(HttpStatus.SC_BAD_REQUEST, "FooBar")
}

class Function extends ApiGatewayHandler with NoopLambdaConfiguration {
  //--- Fields ---
  override protected lazy val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    Logic.handleRequest
  }
}
