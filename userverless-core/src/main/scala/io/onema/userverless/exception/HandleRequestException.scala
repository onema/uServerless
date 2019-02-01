/**
  * This file is part of the ONEMA onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.exception

/**
  *
  * @param message Exception message
  */
class HandledException(message: String) extends LambdaHandlerException(message)

/**
  * Special framework exception used by the API Gateway class to create a response with the given code and
  * message.
  * @param code Http status code, use the org.apache.http.HttpStatus for consistency
  * @param message Exception message
  */
class HandleRequestException(val code: Int, message: String) extends HandledException(message)

class RuntimeException(code: Int, message: String) extends HandleRequestException(code, message)
