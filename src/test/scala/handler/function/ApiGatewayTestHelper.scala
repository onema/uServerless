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

package handler.function

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}

import io.onema.userverless.events.ApiGateway.AwsProxyResponse
import io.onema.userverless.model.ErrorMessage
import io.onema.json.Extensions._


object ApiGatewayTestHelper {

    def toInputStream(obj: Object): InputStream = {
        toInputStream(obj.asJson)
    }

    def toInputStream(str: String): InputStream = {
        new ByteArrayInputStream(str.getBytes())
    }

    def outputToResponse(stream: ByteArrayOutputStream):AwsProxyResponse = {
        stream.toString.jsonDecode[AwsProxyResponse]
    }

    def jsonToErrorMessage(str: String): ErrorMessage = {
        str.jsonDecode[ErrorMessage]
    }

}
