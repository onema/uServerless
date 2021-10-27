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

package functions.simple

import com.amazonaws.services.lambda.runtime.Context
import io.onema.userverless.config.NoopLambdaConfiguration
import io.onema.userverless.service.LambdaHandler

import scala.util.Random

class PositiveRandomFunction extends LambdaHandler[String, Int] with NoopLambdaConfiguration {

  //--- Fields ---
  val rand: Random.type = scala.util.Random
  var current: Int = 0

  //--- Methods ---
  override def execute(event: String, context: Context): Int = {
    current = rand.nextInt
    if(current >= 0) current else current * -1
  }
}
