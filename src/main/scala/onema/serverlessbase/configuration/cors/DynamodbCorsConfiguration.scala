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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, ItemCollection, QueryOutcome}

import scala.util.{Failure, Success, Try}

class DynamodbCorsConfiguration(origin: String, val tableName: String, dynamodbClient: AmazonDynamoDBAsync) extends CorsConfiguration(origin) {

  //--- Fields ---
  private val dynamoDb = new DynamoDB(dynamodbClient)
  private val table = dynamoDb.getTable(tableName)

  //--- Methods ---
  override def isEnabled: Boolean = {
    Try(dynamodbClient.describeTable(tableName)).isSuccess
  }

  override def isOriginValid: Boolean = {
    Try(findOrigin(origin)) match {
      case Success(response) =>
        // If it does find any items in the table, find out if the site is enabled
        if(response.iterator().hasNext) {
          val item = response.iterator().next()
          item.getBOOL("enabled")
        } else {
          false
        }
      case Failure(ex) =>
        throw ex
    }
  }

  private def findOrigin(origin: String): ItemCollection[QueryOutcome]  = {
    val index = table.getIndex("Origin")
    val query = new QuerySpec()
      .withKeyConditionExpression("Origin = :v_origin")
      .withValueMap(new ValueMap().withString(":v_origin", origin))
    index.query(query)
  }
}
