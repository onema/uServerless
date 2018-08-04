/**
  * This file is part of the ONEMA onema.serverlessbase Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package io.onema.serverlessbase.exception

class HandleRequestException(val code: Int, message: String) extends Exception(message)

class RuntimeException(code: Int, message: String) extends HandleRequestException(code, message)
