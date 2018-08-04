/**
  * This file is part of the ONEMA ServerlessBase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package functions.process

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.serverlessbase.configuration.lambda.NoopLambdaConfiguration
import io.onema.serverlessbase.function.LambdaHandler

import scala.collection.JavaConverters._

object Logic {
  def handleEvent(event: SNSEvent, log: Logger): Unit = {
    val records = event.getRecords.asScala
    records.foreach(x => log.info(x.getSNS.getMessage))
  }
}

class Function extends LambdaHandler[Unit] with NoopLambdaConfiguration {
  //--- Fields ---
  override protected val snsClient: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.defaultClient()

  //--- Methods ---
  def lambdaHandler(event: SNSEvent, context: Context): Unit = {
    handle {
      Logic.handleEvent(event, log)
    }
  }


}
