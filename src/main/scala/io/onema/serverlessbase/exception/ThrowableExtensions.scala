/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.serverlessbase.exception

import java.io.{PrintWriter, StringWriter}

object ThrowableExtensions {

  implicit class ExceptionMessage(throwable: Throwable) {

    // --- Methods ---
    /**
      * Converts a class into a json string.
      */
    def message: String = {
      val sw = new StringWriter
      throwable.printStackTrace(new PrintWriter(sw))
      sw.toString
    }
  }
}
