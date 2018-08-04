/**
  * This file is part of the ONEMA serverless-base Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package io.onema.serverlessbase.function

import com.amazonaws.services.lambda.runtime.Context

import scala.util.matching.Regex

object Extensions {
  implicit class ContextExtension(context: Context) {
    def accountId: Option[String] = {
      context.getInvokedFunctionArn
        .split(':').find(x => "[\\d]{12}".r matches x)
    }
  }

  implicit class RichRegex(val underlying: Regex) extends AnyVal {
    def matches(s: String): Boolean = underlying.pattern.matcher(s).matches
  }

}
