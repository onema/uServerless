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

package functions.error

import com.amazonaws.serverless.proxy.model.{AwsProxyRequest, AwsProxyResponse}
import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.exception.HandledException
import io.onema.userverless.function.LambdaHandler

object Logic {
  def handleRequest(request: AwsProxyRequest): AwsProxyResponse = {
    throw new NotImplementedError("FooBar")
  }
}

class Function extends LambdaHandler[String, String] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(request: String, context: Context): String = {
    throw new HandledException("This exception is thrown and logged but is not reported as an error through the Overwatch system")
  }
}
