/**
  * This file is part of the ONEMA Default (Template) Project Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.test

import java.io.{ByteArrayInputStream, InputStream, OutputStream}

import io.onema.userverless.model.ApiGatewayErrorMessage

import scala.reflect.ClassTag

object TestJavaObjectExtensions {
  implicit class StringExtensions(str: String) {
    def toInputStream: InputStream = {
      new ByteArrayInputStream(str.getBytes())
    }

    def toErrorMessage: ApiGatewayErrorMessage ={
      import io.onema.json.Extensions._
      str.jsonDecode[ApiGatewayErrorMessage]
    }
  }

  implicit class AnyClassExtensions(obj: AnyRef) {
    def toInputStream: InputStream = {
      import io.onema.json.JavaExtensions._
      obj.asJson.toInputStream
    }
  }

  implicit class OutputStreamExtensions(stream: OutputStream) {
    def toObject[T: ClassTag]: T = {
      import io.onema.json.JavaExtensions._
      stream.toString.jsonDecode[T]
    }
  }
}
