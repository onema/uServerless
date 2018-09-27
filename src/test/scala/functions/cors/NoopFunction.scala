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

import io.onema.userverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.configuration.cors.{CorsConfiguration, NoopCorsConfiguration}
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.ApiGatewayHandler
import io.onema.userverless.function.ApiGatewayHandler.Cors
import org.apache.http.HttpStatus


class NoopFunction extends ApiGatewayHandler with NoopLambdaConfiguration with Cors {

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): AwsProxyResponse = {
    cors(request) {
      new AwsProxyResponse(HttpStatus.SC_OK)
    }
  }

  /**
    * This method should construct and return the the cors configuration
    *
    * @param origin option containing a string or None with the request origin.
    * @return
    */
  override protected def corsConfiguration(origin: Option[String]): CorsConfiguration = NoopCorsConfiguration()
}
