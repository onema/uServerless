# Event listeners
There are two events that are triggered during the execution of the function:
1. A validation listener
1. A response listener

### Validation listener
Using the `validationListener` method, you can add functions that take the event as a parameter
and return nothing `TEvent => Unit`. These functions can be used to perform validations before the 
function is called.

### Response listener 
Using the `responseListener` method, you can add functions that take a response as a parameter 
and return a response object `TResponse => TResponse` . These functions can be used to modify the response before 
it is written to the `OutputStream`.

### Exception listener
Using the `exceptionListener` method, you can add functions that take a `Throwable` as a parameter
and returns nothing. These functions can be used to report on exceptions, metrics, and more. 
