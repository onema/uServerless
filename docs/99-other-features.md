# Other Features

## Keeping functions warm
Functions can be kept warm by adding a `schedule` event to the functions with the following input:

```yaml

functions:
  success:
    handler: serverless.Function::lambdaHandler
    events:
      # Main trigger
      - sns: some-tpic
      
      # Custom event to keep the function warm
      - schedule:
          rate: rate(5 minutes)
          input:
            warmup: true
```