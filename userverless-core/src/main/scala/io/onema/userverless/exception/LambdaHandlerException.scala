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

package io.onema.userverless.exception

class LambdaHandlerException(message: String) extends Exception(message)

class MessageDecodingException(message: String) extends LambdaHandlerException(message)
