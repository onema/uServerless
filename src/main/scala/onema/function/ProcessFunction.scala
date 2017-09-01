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

package onema.function

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import onema.serverlessbase.core.function.SnsHandler
import scala.collection.JavaConverters._

class ProcessFunction extends SnsHandler {
  override def handleRequest(request: SNSEvent): Unit = {
    val records = request.getRecords.asScala
    records.foreach(x => log.info(x.getSNS.getMessage))
  }
}
