/**
  * This file is part of the ONEMA ServerlessBase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package functions.process

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.typesafe.scalalogging.Logger
import onema.serverlessbase.core.function.SnsHandler

import scala.collection.JavaConverters._

object Logic {
  def handleEvent(event: SNSEvent, log: Logger): Unit = {
    val records = event.getRecords.asScala
    records.foreach(x => log.info(x.getSNS.getMessage))
  }
}

class Function extends SnsHandler {
  def lambdaHandler(event: SNSEvent, context: Context): Unit = {
    handle(() => Logic.handleEvent(event, log))
  }
}
