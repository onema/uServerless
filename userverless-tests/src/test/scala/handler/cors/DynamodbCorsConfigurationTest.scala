/**
  * This file is part of the ONEMA io.onema.userverless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */
package handler.cors

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, DescribeTableResult, GetItemRequest, GetItemResult}
import io.onema.userverless.configuration.cors.DynamodbCorsConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class DynamodbCorsConfigurationTest extends FlatSpec with Matchers with MockFactory {

  "DynamoDB CORS configuration" should "return true when cors config is enabled" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val tableResults = new DescribeTableResult()
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.describeTable(_: String)).expects(*).returning(tableResults)
    val dynamoConfig = DynamodbCorsConfiguration(originSite, "TableName", clientMock)

    // Act
    val isEnabled = dynamoConfig.isEnabled

    // Assert
    isEnabled should be (true)
  }

  "DynamoDB CORS configuration" should "return true when origin is valid" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val getItemResult = new GetItemResult().withItem(Map("Origin" -> new AttributeValue(originSite.get)).asJava)
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).returning(getItemResult)
    val dynamoConfig = DynamodbCorsConfiguration(originSite, "TableName", clientMock)

    // Act
    val isOriginValid = dynamoConfig.isOriginValid

    // Assert
    isOriginValid should be (true)
  }

  /**
    * Dynamo db is not designed to be used as a generic * strategy.  Use EnvCorsConfiguration instead.
    */
  "DynamoDB CORS configuration" should "return FALSE when configured origin is *" in {

    // Arrange
    val originSite = Option("https://foo.com")
    val getItemResult = new GetItemResult().withItem(Map("Origin" -> new AttributeValue("*")).asJava)
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).returning(getItemResult)
    val dynamoConfig = DynamodbCorsConfiguration(originSite, "TableName", clientMock)

    // Act
    val isOriginValid = dynamoConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }

  "DynamoDB CORS configuration that generates an exception" should "re-throw exception" in {

    // Arrange
    val originSite = Option("http://blah.com")
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).throws(new RuntimeException("test"))
    val dynamoConfig = DynamodbCorsConfiguration(originSite, "TableName", clientMock)

    // Act - Assert
    intercept[RuntimeException] { dynamoConfig.isOriginValid }
  }

  "DynamoDB CORS configuration with no values" should "return false for no origin" in {

    // Arrange
    val originSite = Option("http://blah.com")
    val clientMock = mock[AmazonDynamoDBAsync]
    (clientMock.getItem(_: GetItemRequest)).expects(*).returns(new GetItemResult())
    val dynamoConfig = DynamodbCorsConfiguration(originSite, "TableName", clientMock)

    // Act
    val isOriginValid = dynamoConfig.isOriginValid

    // Assert
    isOriginValid should be (false)
  }
}
