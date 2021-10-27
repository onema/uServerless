///**
//  * This file is part of the ONEMA uServerless Package.
//  * For the full copyright and license information,
//  * please view the LICENSE file that was distributed
//  * with this source code.
//  *
//  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
//  *
//  * @author Juan Manuel Torres <software@onema.io>
//  */
//
//package functions.error
//
//import com.amazonaws.services.lambda.runtime.Context
//import io.onema.userverless.config.lambda.NoopLambdaConfiguration
//import io.onema.userverless.service.LambdaHandler
//
//abstract class ExceptionReporter {
//  def report(ex: Throwable): Unit
//}
//
//class FunctionWithListener(val exceptionReporter: ExceptionReporter) extends LambdaHandler[String, Unit] with NoopLambdaConfiguration {
//
//  exceptionListener(ex => {
//    exceptionReporter.report(ex)
//  })
//
//  /**
//    * This method should be implemented by all lambda functions a and is called by the lambda handler. Please note that
//    * the main entry to the lambda codeBlock should be the "lambdaHandler".
//    *
//    * @param event   TEvent
//    * @param context AWS Context
//    * @return TResponse, if not response is required set this to Unit
//    */
//  override def execute(event: String, context: Context): Unit = {
//    throw new RuntimeException(event)
//  }
//}
