# Lambda Configuration
µServerless supports three methods to get configuration values: environment variables, SSM Parameter Store, and Noop

By default the lambda handlers require you to implement the methods defined in the `LambdaConfiguration` trait. 
You can satisfy this requirement by simply extending your function with one of the provided traits: 
* `SsmLambdaConfig`
* `EnvLambdaConfig`
* `NoopLambdaConfig`

`Function`s can retrieve configuration values using the following methods:
* `getValue(name: String): Option[String]`: Name is the fully qualified name and includes the hierarchy of the parameter path and name. For the env vars a value like `/database/password` will be converted to `DATABASE_PASSWORD` 
* `getValues(path: String): Map[String, String]`: Name is a partial path. This will return a map of values containing key -> values for all the elements in the hierarchy. For name like `/database` the method may return all the database related values such as `/database/username`, `/database/password`, etc.

SSM Parameter Store could do a lookup of value names prepending an "environment name" e.g. prod, staging, dev. This is use to be able to create parameters with unique names and reference
them in the function using a consistent name. For example the value `/database/username` for the environment name `prod` will result in a lookup of the parameter `/prod/database/username`.
The environment name is set as the environment variable `STAGE_NAME`.

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
