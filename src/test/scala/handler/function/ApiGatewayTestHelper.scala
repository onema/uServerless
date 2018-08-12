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

import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import io.onema.serverlessbase.model.ErrorMessage


object ApiGatewayTestHelper {

    def toInputStream(obj: Object): InputStream = {

        import io.onema.json.JavaExtensions._
        new ByteArrayInputStream(obj.asJson.getBytes())
    }

    def outputToResponse(stream: ByteArrayOutputStream):AwsProxyResponse = {

        import io.onema.json.JavaExtensions._
        stream.toString.jsonDecode[AwsProxyResponse]
    }

    def jsonToErrorMessage(str: String): ErrorMessage = {

        import io.onema.json.Extensions._
        str.jsonDecode[ErrorMessage]
    }

}
