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

package functions.success

import cats.effect.IO
import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.config.NoopLambdaConfiguration
import io.onema.userverless.service.LambdaHandler

case class MOL(mol: Int)

object LogicIO {
  def handleRequest(request: AwsProxyRequest): IO[AwsProxyResponse] = {
    val response = new AwsProxyResponse(200)
    response.setBody("{\"message\": \"success\"}")
    IO(response)
  }
}

class FunctionIO extends LambdaHandler[AwsProxyRequest, IO[AwsProxyResponse]] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(request: AwsProxyRequest, context: Context): IO[AwsProxyResponse] = {
    LogicIO.handleRequest(request)
  }
}
