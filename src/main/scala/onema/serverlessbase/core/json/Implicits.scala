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

package onema.serverlessbase.core.json

import com.google.gson.Gson

import scala.reflect.ClassTag
import scala.reflect._

object Implicits {
  implicit class AnyClassToJsonString(anyClass: AnyRef) {

    // --- Methods ---
    def toJson: String = {
      val gson = new Gson()
      gson.toJson(anyClass)
    }
  }

  implicit class JsonStringToCaseClass(json: String) {

    //--- Methods ---
    def jsonParse[T: ClassTag]: T = {
      val gson = new Gson()
      gson.fromJson(json, classTag[T].runtimeClass)
    }
  }
}
