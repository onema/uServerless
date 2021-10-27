/*
 * This file is part of the ONEMA userverless-core Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2021-2021, Juan Manuel Torres (http://onema.dev)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.userverless.common

import com.typesafe.scalalogging.Logger
import io.onema.userverless.common.ThrowableExtensions.ExceptionMessage

object LogExtensions {
  implicit class LoggerExtensions(log: Logger) {
    def error(throwable: Throwable, reportException: Boolean = false): Unit = {
      log.error(throwable.structuredMessage(reportException))
    }
  }
}
