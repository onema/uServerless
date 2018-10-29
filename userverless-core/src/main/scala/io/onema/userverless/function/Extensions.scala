/**
  * This file is part of the ONEMA serverless-base Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.function

import com.amazonaws.services.lambda.runtime.Context

import scala.util.matching.Regex

object Extensions {
  implicit class ContextExtension(context: Context) {

    /**
      * Get the account id from the Context
      *
      * @return Option[String]
      */
    def accountId: Option[String] = {
      context.getInvokedFunctionArn
        .split(':').find(x => "[\\d]{12}".r matches x)
    }
  }

  implicit class RichRegex(val regex: Regex) extends AnyVal {

    /**
      * Extension method for regex to match a string
      *
      * @param s the string that will match against
      * @return Boolean
      */
    def matches(s: String): Boolean = regex.pattern.matcher(s).matches
  }
}
