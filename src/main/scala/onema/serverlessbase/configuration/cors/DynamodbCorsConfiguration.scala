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

  def apply(origin: String): DynamodbCorsConfiguration = DynamodbCorsConfiguration(origin, "CorsOrigins")

  def apply(origin: String, tableName: String): DynamodbCorsConfiguration = {
    DynamodbCorsConfiguration(origin, tableName, AmazonDynamoDBAsyncClientBuilder.defaultClient())
  }

  def apply(origin: String, tableName: String, dynamodbClient: AmazonDynamoDBAsync): DynamodbCorsConfiguration = {
    new DynamodbCorsConfiguration(origin, tableName, AmazonDynamoDBAsyncClientBuilder.defaultClient())
  }
}

class DynamodbCorsConfiguration(origin: String, val tableName: String, dynamodbClient: AmazonDynamoDBAsync) extends CorsConfiguration(origin) {

  //--- Fields ---
  private val dynamoDb = new DynamoDB(dynamodbClient)
  private val table = dynamoDb.getTable(tableName)

  //--- Methods ---
  override def isEnabled: Boolean = {
    Try(dynamodbClient.describeTable(tableName)).isSuccess
  }

  override def isOriginValid: Boolean = {
    if(Option(origin).isEmpty) return false
    Try(findOrigin) match {
      case Success(response) =>
        // If it does find any items in the table, find out if the site is enabled
        if(response.isDefined) {
          response.get == origin
        } else {
          false
        }
      case Failure(ex) =>
        throw ex
    }
  }

  private def findOrigin: Option[String]  = {
    Option(table.getItem("Origin", origin)) match {
      case Some(item) => Some(item.getString("Origin"))
      case None => None
    }
  }
}
