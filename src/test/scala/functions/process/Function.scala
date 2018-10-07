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
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.typesafe.scalalogging.Logger
import io.onema.userverless.configuration.lambda.NoopLambdaConfiguration
import io.onema.userverless.function.LambdaHandler

import scala.collection.JavaConverters._

object Logic {
  def handleEvent(event: SNSEvent, log: Logger): Unit = {
    val records = event.getRecords.asScala
    records.foreach(x => log.info(x.getSNS.getMessage))
  }
}

class Function extends LambdaHandler[SNSEvent, Unit] with NoopLambdaConfiguration {

  //--- Methods ---
  def execute(event: SNSEvent, context: Context): Unit = {
    Logic.handleEvent(event, log)
  }
}
