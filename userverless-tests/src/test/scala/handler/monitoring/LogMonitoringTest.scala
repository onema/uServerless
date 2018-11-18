/**
  * This file is part of the ONEMA uServerless Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package handler.monitoring

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import io.onema.userverless.monitoring.LogMetrics._

class LogMonitoringTest extends FlatSpec with Matchers with MockFactory {

  "Variadic parameter for custom tags" should "return default and custom tags" in {
    // Arrange
    val customTags = Seq(("foo", "bar"), ("baz", "blah"))

    // Act
    val results = getTags(customTags)

    // Assert
    results should be ("#stage:dev,foo:bar,baz:blah")

  }

  "Variadic parameter with no values" should "return default tags only" in {
    // Arrange
    val customTags = Seq()

    // Act
    val results = getTags(customTags)

    // Assert
    results should be ("#stage:dev")

  }
}
