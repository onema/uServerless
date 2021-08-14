/**
  * This file is part of the ONEMA uServerless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2021, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.config.lambda

import cats.data.ValidatedNec
import cats.implicits.catsSyntaxValidatedIdBinCompat0

import scala.util.{Failure, Success, Try}

sealed trait LambdaConfigValidation {
  def message: String
}

case class ValueNotFound(path: String) extends LambdaConfigValidation {
  override def message: String = s"""No value could be found for path: "$path"."""
}

case class ValueIsNotANumber(path: String, exception: NumberFormatException) extends LambdaConfigValidation {
  override def message: String =
    s"""The value ${exception.getMessage} is not a number or cannot be converted to a number for path: "$path"."""
}

case class PathGeneratedException(path: String, exception: Throwable) extends LambdaConfigValidation {
  override def message: String =
    s"""An error occurred when getting the value for path: "$path".
       |
       | Message: ${exception.getMessage}
       |
       | Cause: ${exception.getCause}
       |""".stripMargin
}

trait Configuration {

  type ValidationResult[A] = ValidatedNec[LambdaConfigValidation, A]

  /**
    * Get config values by name. This assumes that the name uses a path notation
    * @param path name of the config value
    * @return
    */
  def getValue(path: String): Option[String]

  /**
    * Get multiple values using a prefix.
    * e.g. the path "/database" would return /database/username, /database/password, /database/port, /database/host, etc.
    * @param path name of the path to search for
    * @return
    */
  def getValues(path: String): Map[String, String]

  /**
    * Get a single value from the config and return a validation result. This method can be used to accumulate
    * and return multiple errors.
    * @param path name of the path to search for
    * @return ValidationResult
    */
  def validate(path: String): ValidationResult[String] = validateToType(path) { s => s }
  def validateInt(path: String): ValidationResult[Int] = validateToType(path) { stringVal => stringVal.toInt }
  def validateFloat(path: String): ValidationResult[Float] = validateToType(path) { stringVal => stringVal.toFloat }
  def validateDouble(path: String): ValidationResult[Double] = validateToType(path) { stringVal => stringVal.toDouble }

  /**
    * This method uses the getValue to retrieve the value from the path. Additionally, it takes a conversion function
    * @param path name of the path to search for
    * @param conversion conversion function to transform value to a new type
    * @tparam A Type that the parameter value will be converted to
    * @return ValidationResult
    */
  def validateToType[A](path: String)(conversion: String => A): ValidationResult[A] = {
    Try {
      getValue(path).map(conversion)
    } match {
      case Success(Some(result))                     => result.validNec
      case Success(None)                             => ValueNotFound(path).invalidNec
      case Failure(exception: NumberFormatException) => ValueIsNotANumber(path, exception).invalidNec
      case Failure(exception: Throwable)             => PathGeneratedException(path, exception).invalidNec
    }
  }
}
