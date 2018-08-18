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
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import io.onema.serverlessbase.function.{CronJobHandler, LambdaHandler}

object ScheduledLogic {
  def handleEvent(event: ScheduledEvent, log: Logger): Unit = {
    log.info(event.getId)
  }
}

class ScheduledFunction extends LambdaHandler[ScheduledEvent, Unit] with NoopLambdaConfiguration {
  //--- Fields ---
  override protected lazy val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  override def execute(event: ScheduledEvent, context: Context): Unit = {
    ScheduledLogic.handleEvent(event, log)
  }
}
