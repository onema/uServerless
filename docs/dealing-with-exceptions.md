# Dealing with exceptions
```scala
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.{AmazonSNSAsync, AmazonSNSAsyncClientBuilder}
import com.typesafe.scalalogging.Logger
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.config.lambda.EnvLambdaConfiguration
import io.onema.userverless.http.HttpStatus

object Logic {
  val log = Logger("logic")
  def handleRequest(event: SNSEvent): Unit = {
    throw new RuntimeException("There was a problem!")
  }
}

class Function extends LambdaHandler[SNSEvent, Unit] with EnvLambdaConfiguration {

  //--- Methods ---
  def execute(snsEvent: SNSEvent, context: Context): Unit = {
    Logic.handleRequest(snsEvent)
  }
}
```

There are a few things to notice here:
 1. The function uses the `EnvLambdaConfiguration` trait. This will enable the handler to get the `SNS_ERROR_TOPIC` 
 environment variable. This is the topic that will be use to report the error. 
     * If we used the `SsmLambdaConfiguration` the name of the error topic is in the parameter `/sns/error/topic`.
     * If we used the `NoopLambdaConfig` errors will never get reported.
 1. The lambda handler rethrows the error after it has been reported.
 
 
### Enable error notifications
If you want to notify a subsystem when lambda functions are failing, you have two options
1. Launch the Overwatch app before launching any of your lambda functions. The Overwatch will report errors of all your µServerless functions to a custom SNS Topic
2. Register a function using the `exceptionListener` method. In this function you can add custom logic to report the exception in any way you want

```scala
  exceptionListener(ex: Throwable => {
    // Exception reporter is a custom 
    snsClient.publish("myCustomTopic", ex.getMessage)
  })
```
