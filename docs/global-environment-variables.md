# Global Environment Variables

All functions should properly set the following environment variables

  * `REPORT_EXCEPTION`: false
  * `LOG_LEVEL`: DEBUG
  * `STAGE_NAME`: `${self:custom.stageName}`
  * `APP_NAME`: `${self:service}`
 
 While they are not required, they are used by µServerless to report logs, metrics, errors and notifications.
 
 ## REPORT_EXCEPTION
 If the Overwatch is installed in your system, this value can be set to `true` to make sure exceptions are reported when 
 they occur. All errors, except for those who inherit from `HandledException` are reported to the Overwatch.
 
 Valid values are `true` or `false` by default is false.
 
 ## LOG_LEVEL
 This is the log level that will be displayed in the CloudWatch logs.
 
 ## STAGE_NAME
 This is any name you want to give to your application such as `production`, `prod`, `stage`, `Test`, `dev`, `username`.
 In the exaple above the stage name has been set in the custom section of the serverless.yml file. 
 
 ## APP_NAME
 This is the serverless application name and cam be referenced via serverless variables.
 