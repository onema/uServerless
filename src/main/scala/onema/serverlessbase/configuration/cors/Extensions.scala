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

package onema.serverlessbase.configuration.cors

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse


object Extensions {
  implicit class AwsProxyResponseExtension(response: AwsProxyResponse) {
    def withCors(corsConfiguration: CorsConfiguration): AwsProxyResponse = {
      if(corsConfiguration.isOriginValid) {
        response.addHeader("Access-Control-Allow-Origin", corsConfiguration.origin)
      }
      response
    }
  }
}
