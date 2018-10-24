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

package functions.process

import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.scalalogging.Logger
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.SnsHandler

object Logic {
  def handleEvent(eventMessage: String, log: Logger): Unit = {
    log.info(eventMessage)
  }
}

class Function extends SnsHandler[String] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(event: String, context: Context): Unit = {
    Logic.handleEvent(event, log)
  }
}
