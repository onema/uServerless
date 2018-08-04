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

package io.onema.serverlessbase.configuration.lambda

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementAsync
import com.amazonaws.services.simplesystemsmanagement.model.{GetParameterRequest, GetParametersByPathRequest, ParameterNotFoundException}
import io.onema.serverlessbase.configuration.lambda.SsmLambdaConfiguration.StringExtensions

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object SsmLambdaConfiguration {
  implicit class StringExtensions(value: String) {

    //--- Methods ---
    def stripDoubleSlashes: String = {
      value.replaceAll("///", "/").replaceAll("//", "/")
    }
  }
}

trait SsmLambdaConfiguration extends LambdaConfiguration {

  //--- Fields ---
  protected val ssmClient: AWSSimpleSystemsManagementAsync

  protected val stageName: String = sys.env.getOrElse("STAGE_NAME", "")

  //--- Methods ---
  /**
    * Get SSM parameter store values by name. This assumes that the name uses a path notation and that all parameters
    * have been prepended with the environment name
    * e.g. the path "/database/username" for the environment prod would resolve to "/prod/database/username"
    * @param path name of the parameter
    * @return
    */
  def getValue(path: String): Option[String] = {
    val name = s"/$stageName/$path".stripDoubleSlashes
    tryParameter(name)
  }

  /**
    * Get SSM Parameter store values by path. This gets all the values that start with a specific path including the
    * environment name.
    * e.g. the path "/database" for the environment prod would return "/prod/database/username",
    *  "/prod/database/password", "/prod/database/port", etc.
    * @param path name of the path to search for
    * @return
    */
  def getValues(path: String): Map[String, String] = {
    @tailrec
    def getParameters(path: String, params: Map[String, String] = Map(), nextToken: Option[String] = None): Map[String, String]  = {
      val name = s"/$stageName/$path".stripDoubleSlashes
      val request = new GetParametersByPathRequest()
        .withPath(name)
        .withRecursive(true)
        .withWithDecryption(true)
      if (nextToken.isDefined) request.withNextToken(nextToken.get)
      val response = ssmClient.getParametersByPath(request)
      val current = response.getParameters.asScala.map(x => x.getName.replaceAll(s"/$stageName", "") -> x.getValue).toMap

      // Return params, otherwise continue with the recursion
      if (Option(response.getNextToken).isEmpty) {
        params ++ current
      } else {
        getParameters(path, params ++ current, Option(response.getNextToken))
      }
    }
    getParameters(path)
  }

  private def tryParameter(name: String): Option[String] = {
    val response = Try(ssmClient.getParameter(
      new GetParameterRequest()
        .withName(name)
        .withWithDecryption(true)
    ))
    response match {
      case Success(r) => Some(r.getParameter.getValue)
      case Failure(_: ParameterNotFoundException) => None
      case Failure(e) => throw e
    }
  }
}
