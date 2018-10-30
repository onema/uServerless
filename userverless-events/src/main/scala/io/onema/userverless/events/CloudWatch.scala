/**
  * This file is part of the ONEMA uServerlessEvents Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.events

object CloudWatch {
  case class ScheduledEvent(
    version: Option[String] = None,
    account: String,
    region: String,
    detail: Map[String, Any],
    `detail-type`: String,
    source: String,
    time: String,
    id: String,
    resources: List[String]
  )
}
