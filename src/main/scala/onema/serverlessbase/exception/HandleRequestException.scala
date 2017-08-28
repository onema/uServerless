/**
  * This file is part of the ONEMA onema Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2017, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.exception

import java.util

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import onema.serverlessbase.model.ErrorMessage
import onema.serverlessbase.core.json.Implicits.AnyClassToJsonString

class HandleRequestException(val code: Int, message: String) extends Exception(message) {
  def httpResponse: AwsProxyResponse = {
    new AwsProxyResponse(
      code,
      new util.HashMap[String, String](),
      ErrorMessage(getMessage).toJson
    )
  }
}

class RuntimeException(code: Int, message: String) extends HandleRequestException(code, message)
