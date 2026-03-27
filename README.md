# imposter

A Java library for AWS Lambda that automatically identifies and deserializes the AWS event source of an inbound `InputStream`, enabling a single Lambda function to handle multiple event types.

## The Problem

AWS Lambda's `RequestStreamHandler` receives a raw `InputStream`. When a Lambda is triggered by multiple sources (API Gateway, SQS, S3, etc.), you must manually parse and identify the event type before you can work with it. This is boilerplate that `imposter` eliminates.

## Supported Event Types

| Event Source | Class |
|---|---|
| API Gateway REST (v1) | `APIGatewayProxyRequestEvent` |
| API Gateway HTTP (v2) | `APIGatewayV2HTTPEvent` |
| Application Load Balancer | `ApplicationLoadBalancerRequestEvent` |
| SQS | `SQSEvent` |
| DynamoDB Streams | `DynamodbEvent` |
| S3 | `S3Event` |
| CloudFront (Lambda@Edge) | `CloudFrontEvent` |
| SNS | `SNSEvent` |
| EventBridge / Scheduled Events | `ScheduledEvent` |
| Kinesis | `KinesisEvent` |

## Installation

**Gradle:**
```groovy
dependencies {
    implementation 'com.wemojema:imposter:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.wemojema</groupId>
    <artifactId>imposter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

Extend `AbstractRequestStreamHandler` and override the `handle()` method for each event type you want to handle:

```java
public class MyLambdaHandler extends AbstractRequestStreamHandler {

    @Override
    public void handle(SQSEvent event) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            System.out.println("SQS message: " + message.getBody());
        }
    }

    @Override
    public void handle(APIGatewayV2HTTPEvent event) {
        String path = event.getRawPath();
        // write response to this.outputStream
    }

    @Override
    public void handle(S3Event event) {
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        System.out.println("S3 bucket: " + bucket);
    }
}
```

The `outputStream` and `context` fields are available from `AbstractRequestStreamHandler` for writing responses.

### Handling Unknown Events

If an unrecognized event arrives, `handleUnknown(InputStream input)` is called. By default it throws `UnknownInputStreamSourceException`. You can override it to handle unexpected inputs gracefully:

```java
@Override
public void handleUnknown(InputStream input) {
    // log and ignore, or write a fallback response
}
```

## How It Works

`imposter` reads the incoming JSON once, parses it into a structural map, and inspects key fields to identify the event source, checks for the presence and value of discriminating keys that are unique to each event type (e.g., `Records[0].eventSource == "aws:sqs"` for SQS). Once identified, the JSON is deserialized into the appropriate AWS event class using Jackson.

## Adding to an Existing Lambda

Register `MyLambdaHandler` as your Lambda handler class. No other changes are needed — your existing trigger configuration in AWS stays the same.

## Requirements

- Java 8+
- AWS Lambda Java runtime

## License

Apache 2.0
