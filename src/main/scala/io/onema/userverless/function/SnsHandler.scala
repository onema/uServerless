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

package io.onema.userverless.function

import io.onema.userverless.events.Sns.SnsEvent
import io.onema.json.Extensions._
import io.onema.userverless.exception.MessageDecodingException

import scala.reflect._

abstract class SnsHandler[TEvent: Manifest] extends LambdaHandler[TEvent, Unit]{
  override protected def decodeEvent(json: String): TEvent = {
    val record = json.jsonDecode[SnsEvent].Records.head
    record.Sns.Message.map(x => x.jsonDecode[TEvent]) match {
      case Some(event: TEvent) => event
      case None => throw new MessageDecodingException("No message available for decoding")
    }
  }
}
