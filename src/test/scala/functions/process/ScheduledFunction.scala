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
import io.onema.userverless.events.CloudWatch.ScheduledEvent
import io.onema.userverless.function.LambdaHandler

object ScheduledLogic {
  def handleEvent(event: ScheduledEvent, log: Logger): Boolean = {
    log.info(event.id)
    true
  }
}

class ScheduledFunction extends LambdaHandler[ScheduledEvent, Boolean] with NoopLambdaConfiguration {

  //--- Methods ---
  override def execute(event: ScheduledEvent, context: Context): Boolean = {
    ScheduledLogic.handleEvent(event, log)
  }
}
