#Logging
By default µServerless uses logback to configure logger behavior. as of version `0.0.2` it structures log messages as JSON
using the `logstash-logback-encoder`. In addition to the default logged values 

An log.info like `log.info("This is my message")` Will result in the following log message:
```
*** INFO: {
  "@timestamp": "2018-11-11T16:52:47.502-08:00",
  "@version": "1",
  "message": "This is my message",
  "logger_name": "com.example.App.Function",
  "thread_name": "ScalaTest-run-running-LambdaHandler",
  "level": "INFO",
  "level_value": 20000,
  // Custom values added to each message by the default configuration
  "type": "log",
  "stage": "test",
  "function": "lambda-function-name",
  "lambda_version": "$LATEST"
}
```

### Adding custom properties to the log message 
You may want to attach additional information to your log message in oder to be able to find it better if you forward these emails to a custom db like elastic search. 
To add custom properties to include the use the `keyValue` from the logstash StructuredArguments:
```scala
import net.logstash.logback.argument.StructuredArguments._
...

log.info("This is my message", keyValue("Foo", "bar"), keyValue("BAZ", "BLAH"))
```
This will result in the following message:
```
*** INFO: {
  "@timestamp": "2018-11-11T16:52:47.502-08:00",
  "@version": "1",
  "message": "This is my message",
  "logger_name": "com.example.App.Function",
  "thread_name": "ScalaTest-run-running-LambdaHandler",
  "level": "INFO",
  "level_value": 20000,
  
  // Values added to each message by the default configuration
  "type": "log",
  "stage": "test",
  "function": "lambda-function-name",
  "lambda_version": "$LATEST",
  
  // Custom values
  "Foo": "bar",
  "BAZ": "BLAH"
}
```

### Error logging:
Error logs are handled by a custom log appender. This is to enable µServerless to build a custom error payload when 
exception occur that containing the stack trace.

```
*** ERROR : {
  "message": "Origin 'http://baz.com' is not authorized",
  "exceptionClass": "io.onema.userverless.exception.HandleRequestException",
  "stackTrace": [
    {
      "fileName": "ApiGatewayHandler.scala",
      "lineNumber": 95,
      "className": "io.onema.userverless.function.ApiGatewayHandler$Cors",
      "methodName": "cors()"
    },
    {
      "fileName": "ApiGatewayHandler.scala",
      "lineNumber": 83,
      "className": "io.onema.userverless.function.ApiGatewayHandler$Cors",
      "methodName": "cors$()"
    },
    ...
  ],
  "@timestamp": "2018-11-11T16:52:47.781-0800",
  "type": "exception",
  "stage": "test",
  "function": "lambda-function-name"",
  "lambdaVersion": "$LATEST""
}
```
If you use the error method directly, you will only see a message with the following format:
```
*** ERROR: %msg%n
```
### Metrics
Metrics is a way to log a Statsd compatible message. There are two types of metrics available: `timer` and `count`.

Timers are available as a code-block (thunk):
```scala
import io.onema.userverless.monitoring.LogMetrics.time

time("NameOfMyEvent") {
  val thisIsMyCode = "Foo"
}
```
This will result in the following message:
```
*** METRIC: {
  "@timestamp": "2018-11-11T16:52:47.748-08:00",
  "@version": "1",
  "message": "NameOfMyEvent:0|ms|@1|#stage:test",
  "logger_name": "io.onema.userverless.monitoring.Metrics",
  "thread_name": "ScalaTest-run-running-ApiGatewayHandlerWithCorsTest",
  "level": "INFO",
  "level_value": 20000,
  "metric_name": "NameOfMyEvent",
  "metric_type": "time",
  "type": "metric",
  "stage": "test",
  "function": "lambda-function-name"",
  "lambda_version": "$LATEST""
}
```

A count can be added like such:
```scala
import io.onema.userverless.monitoring.LogMetrics.count

count("NameOfCountEvent")
```
This will result in a log containing the following message:
`NameOfMyEvent:1|c|@1|#stage:test"`

> *NOTE:*
> 
> The metrics are only logged and are not submitted to CloudWatch. To get the metrics reported to CloudWatch you need 
> to use the Overwatch app 

#### Adding custom tags to your metrics
To submit custom tags in your metrics, pass key value pairs to the functions:

```scala
count("CounterName", ("MyKey", "MyValue"), ("AnotherKey", "AnotherValue"))
time("TimerName", ("MyKey", "MyValue"), ("AnotherKey", "AnotherValue"))
```

### Selecting the log level
By default the log level is set to DEBUG, you can overwrite this by setting the `LOG_LEVEL` **environment variable** to one of the log levels:
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`

### Override log configuration
Simply add a `src/main/resources/logback.xml` with your custom configuration and add a custom merging strategy to your `build.sbt`:
```scala
assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
```