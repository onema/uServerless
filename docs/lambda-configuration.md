# Lambda Configuration
µServerless supports three methods to get configuration values: environment variables, SSM Parameter Store, and Noop

By default the lambda handlers require you to implement the methods defined in the `LambdaConfiguration` trait. 
You can satisfy this requirement by simply extending your function with one of the provided traits: 

* `SsmLambdaConfig`
* `EnvLambdaConfig`
* `NoopLambdaConfig`

## Usage
Functions can retrieve configuration values using the following methods:

### Get Values
```
getValue(name: String): Option[String]
``` 
Name is the fully qualified name and includes the hierarchy of the parameter path and name. For the env vars a value like `/database/password` will be converted to `DATABASE_PASSWORD` 

### Get Paths
```
getValues(path: String): Map[String, String]
``` 
Here the path is a partial path. This will return a map of values containing key -> values for all the elements in the hierarchy. For name like `/database` the method may return all the database related values such as `/database/username`, `/database/password`, etc.
## EnvLambdaConfig
This trait comes bundled with µServerless and can be used by extending the `EnvLambdaConfiguration` trait like such
```scala
class TestFunction(snsClientMock: AmazonSNSAsync) extends LambdaHandler with EnvLambdaConfiguration {
  val foo: Option[String] = getValue("/foo")
  val barHierarchy: Map[String, String] = getValues("/bar/")
}
```

## SsmLambdaConfig
This configuration gets values from the AWS SSM parameter store. Encrypted values will be automatically decrypted. 

### Unique values across stages 
SSM Parameter Store could do a lookup of value names prepending a "stage name" e.g. prod, staging, dev. 
This is use to be able to create parameters with unique names and reference
them in the function using a consistent name. For example the value `/database/username` for the stage name `prod` will 
result in a lookup of the parameter `/prod/database/username`.

The environment name is set via the environment variable `STAGE_NAME`.

### Installation
SSM Parameter store configuration must be installed using the `userverless-ssm-config` package:
```
libraryDependencies += "io.onema" %% "userverless-ssm-config" % "<LATEST_VERSION>"
```

To use the SSM environment variable in  your function simply extend from the `SsmLambdaConfiguration` trait like such

```scala
class TestFunction(snsClientMock: AmazonSNSAsync) extends LambdaHandler with SsmLambdaConfiguration {
  val foo: Option[String] = getValue("/foo")
  val barHierarchy: Map[String, String] = getValues("/bar/")
}
```

## NoopLambdaConfig
This trait always returns `None` for `getValue` and `Seq()` for `getValues`.

## Custom implementations
You may create your own custom implementations by extending the `LambdaConfiguration` trait and implementing `getValue` and `getValues`.
