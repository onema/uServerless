/**
  * This file is part of the ONEMA userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package functions.simple

import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.LambdaHandler


class EchoFunction extends LambdaHandler[Int, String] with NoopLambdaConfiguration {

  //--- Methods ---
  override def execute(event: Int, context: Context): String = {
    event.toString
  }
}
