µServerless for Scala
=====================
[![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiQnkzdWlQWXJ0cWtDTjl5M2J3OGJJYTRtK01vVXJMazBRanhidXdUbkVUejI4Y1g4WUhoK3k4cWRUV1NSOUl4RzJ0VFMvSy9Xb3BYTGN6eUUyM2ZsMlRZPSIsIml2UGFyYW1ldGVyU3BlYyI6IlFrTncvSHplNTRYUk1iRjciLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)](https://console.aws.amazon.com/codebuild/home?region=us-east-1#/projects/uServerless/view)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3ad4503004f440fabd69c6fb71cd8225)](https://www.codacy.com/gh/onema/uServerless/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=onema/uServerless&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/3ad4503004f440fabd69c6fb71cd8225)](https://www.codacy.com/gh/onema/uServerless/dashboard?utm_source=github.com&utm_medium=referral&utm_content=onema/uServerless&utm_campaign=Badge_Coverage)
[![Documentation Status](https://readthedocs.org/projects/userverless/badge/?version=latest)](https://userverless.readthedocs.io/en/latest/?badge=latest)
[![LICENSE](https://img.shields.io/badge/license-Apache--2.0-blue.svg?longCache=true&style=flat-square)](LICENSE)

The µServerless package (pronounced micro-serverless) is a small collection of classes, traits and adapters to help you
build AWS Lambda functions using scala. 

## Install
Add the following to your SBT config:

```
libraryDependencies += "io.onema" %% "userverless-core" % "<LATEST_VERSION>"
```

## Overwatch
All your lambda functions build on top of µServerless log information about errors, metrics, and different log levels. 
The Overwatch listens for these logs and parses them reporting metrics and errors. The Overwatch is installed independently 
from your applicaiton, and you can find more information in the GitHub repository [µServerless Overwatch](https://github.com/onema/uServerlessOverwatch)

> **NOTE**:
>
> The Overwatch is not required to use µServerless, but it will enable you to have greater visibility into your applications

## Documentation
For more information see the [documentation section](/docs) 