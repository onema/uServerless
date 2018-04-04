/**
  * This file is part of the ONEMA Default (Template) Project Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <kinojman@gmail.com>
  */

package onema.serverlessbase.configuration.cors

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}

import scala.util.{Failure, Success, Try}

object DynamodbCorsConfiguration {

  def apply(origin: Option[String]): DynamodbCorsConfiguration = DynamodbCorsConfiguration(origin, "CorsOrigins")

  def apply(origin: Option[String], tableName: String): DynamodbCorsConfiguration = {
    DynamodbCorsConfiguration(origin, tableName, AmazonDynamoDBAsyncClientBuilder.defaultClient())
  }

  def apply(origin: Option[String], tableName: String, dynamodbClient: AmazonDynamoDBAsync): DynamodbCorsConfiguration = {
    new DynamodbCorsConfiguration(origin, tableName, AmazonDynamoDBAsyncClientBuilder.defaultClient())
  }
}

class DynamodbCorsConfiguration(origin: Option[String], val tableName: String, dynamodbClient: AmazonDynamoDBAsync) extends CorsConfiguration(origin) {

  //--- Fields ---
  private val dynamoDb = new DynamoDB(dynamodbClient)
  private val table = dynamoDb.getTable(tableName)

  //--- Methods ---
  override def isEnabled: Boolean = {
    Try(dynamodbClient.describeTable(tableName)).isSuccess
  }

  override def isOriginValid: Boolean = {
    origin match {
      case Some(originValue) =>
        Try(findOrigin) match {
          case Success(response) =>
            // If it does find any items in the table, find out if the site is enabled
            response.contains(originValue)
          case Failure(ex) =>
            throw ex
        }
      case None => false
    }
  }

  private def findOrigin: Option[String]  = {
    Option(table.getItem("Origin", origin.getOrElse(""))) match {
      case Some(item) => Some(item.getString("Origin"))
      case None => None
    }
  }
}
