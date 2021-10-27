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

package io.onema.userverless.service

import io.onema.json.Extensions._
import io.onema.userverless.events.Sns.SnsEvent
import io.onema.userverless.exception.MessageDecodingException

import scala.reflect.Manifest

abstract class SnsHandler[TEvent: Manifest] extends LambdaHandler[TEvent, Unit]{
  override protected def jsonDecode(json: String): TEvent = {
    log.debug("DECODING SNS MESSAGE")
    val records = json.jsonDecode[SnsEvent].Records
    records match {
      case record::Nil =>
        log.debug("DECODING SNS MESSAGE RECORD")
        record.Sns.Message.map(_.jsonDecode[TEvent]) match {
          case Some(event: TEvent) => event
          case None => throw new MessageDecodingException("No message class available for decoding")
        }
      case _ =>
        log.debug("ERROR DECODING SNS MESSAGE")
        throw new MessageDecodingException("The SNS Event contains no records, this should never happen!")
    }
  }
}
