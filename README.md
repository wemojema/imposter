# imposter
A Java Library to identify the AWS origin of an inbound InputStream

## Why do I need imposter?
AWS Lambda has a single entry point function, this entry point method has a set of strongly typed arguments. Doppelganger identifies a loosely types InputStream into the appropriate strongly types corresponding Java object, thus allowing a single aws lambda function to be triggered from multiple sources without the need to create multiple functions.
