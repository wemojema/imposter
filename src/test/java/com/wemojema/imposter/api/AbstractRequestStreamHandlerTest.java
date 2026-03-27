package com.wemojema.imposter.api;

import com.amazonaws.services.lambda.runtime.events.*;
import com.wemojema.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class AbstractRequestStreamHandlerTest extends AbstractTest {
    InputStream apiGatewayProxyEventInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/apigateway-aws-proxy.json")
            , "sample json InputStream cannot be null");

    InputStream sqsInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/sqs-event.json")
            , "sample json InputStream cannot be null"
    );
    InputStream ddbInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/dynamodb.json")
            , "sample json InputStream cannot be null"
    );
    InputStream httpApiGateway = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/http-apigateway.json")
            , "sample http api gateway cannot be null"
    );
    InputStream s3InputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/s3event.json")
            , "sample json InputStream cannot be null"
    );
    InputStream albInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/alb-event.json")
            , "sample json InputStream cannot be null"
    );
    InputStream snsInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/sns-event.json"),
            "sample sns InputStream cannot be null");
    InputStream eventBridgeInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/eventbridge-event.json"),
            "sample eventbridge InputStream cannot be null");
    InputStream kinesisInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/kinesis-event.json"),
            "sample kinesis InputStream cannot be null");
    InputStream cloudFrontInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/cloudfront-event.json"),
            "sample cloudfront InputStream cannot be null");


    class UUT extends AbstractRequestStreamHandler {
        String invokedMethod;

        @Override
        public void handle(APIGatewayV2HTTPEvent event) {
            this.invokedMethod = "handle(APIGatewayV2HTTPEvent event)";
        }

        @Override
        public void handle(SQSEvent event) {
            assert event != null;
            assert event.getRecords() != null;
            this.invokedMethod = "handle(SQSEvent event)";
        }

        @Override
        public void handle(DynamodbEvent event) {
            this.invokedMethod = "handle(DynamodbEvent event)";
        }

        @Override
        public void handle(S3Event event) {
            this.invokedMethod = "handle(S3Event event)";
        }

        @Override
        public void handle(ApplicationLoadBalancerRequestEvent event) {
            this.invokedMethod = "handle(ApplicationLoadBalancerRequestEvent event)";
        }

        @Override
        public void handle(APIGatewayProxyRequestEvent event) {
            this.invokedMethod = "handle(APIGatewayProxyRequestEvent event)";
        }

        @Override
        public void handle(SNSEvent event) {
            this.invokedMethod = "handle(SNSEvent event)";
        }

        @Override
        public void handle(ScheduledEvent event) {
            this.invokedMethod = "handle(ScheduledEvent event)";
        }

        @Override
        public void handle(KinesisEvent event) {
            this.invokedMethod = "handle(KinesisEvent event)";
        }

        @Override
        public void handle(CloudFrontEvent event) {
            this.invokedMethod = "handle(CloudFrontEvent event)";
        }

        @Override
        public void handleUnknown(InputStream input) {
            this.invokedMethod = "handleUnknown";
        }
    }

    UUT uut;

    public static class Pojo {
        public String test;

        public Pojo(String test) {
            this.test = test;
        }

    }

    @BeforeEach
    void setUp() {
        uut = new UUT();
    }

    // C8: The base handleUnknown throws UnknownInputStreamSourceException.
    // We use a separate anonymous handler (not UUT) to verify the exception is still thrown.
    @Test
    void should_throw_UnknownInputStreamException_when_it_cannot_determine_the_InputStream_source() {
        AbstractRequestStreamHandler handler = new AbstractRequestStreamHandler() {};
        Assertions.assertThrows(UnknownInputStreamSourceException.class,
                () -> handler.handleRequest(inputStreamOf(new Pojo("testing")), new ByteArrayOutputStream(), null));
    }

    @Test
    void should_identify_an_APIGatewayProxyEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(apiGatewayProxyEventInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(APIGatewayV2HTTPEvent event)", uut.invokedMethod);
    }

    @Test
    void should_identify_an_SQSEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(sqsInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(SQSEvent event)", uut.invokedMethod);
    }

    @Test
    void should_identify_a_DynamodbEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(ddbInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(DynamodbEvent event)", uut.invokedMethod);
    }

    @Test
    void should_identify_as_an_apigateway_event_when_provided_http_apigw_events() {
        uut.handleRequest(httpApiGateway, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(APIGatewayProxyRequestEvent event)", uut.invokedMethod);
    }

    // C5: Re-enabled S3 test
    @Test
    void should_identify_a_S3Event_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(s3InputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(S3Event event)", uut.invokedMethod);
    }

    @Test
    void should_identify_an_ApplicationLoadBalancerEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(albInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(ApplicationLoadBalancerRequestEvent event)", uut.invokedMethod);
    }

    // C6: SNS event
    @Test
    void should_identify_an_SNSEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(snsInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(SNSEvent event)", uut.invokedMethod);
    }

    // C6: EventBridge / ScheduledEvent
    @Test
    void should_identify_a_ScheduledEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(eventBridgeInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(ScheduledEvent event)", uut.invokedMethod);
    }

    // C6: Kinesis event
    @Test
    void should_identify_a_KinesisEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(kinesisInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(KinesisEvent event)", uut.invokedMethod);
    }

    // C6: CloudFront event
    @Test
    void should_identify_a_CloudFrontEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(cloudFrontInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(CloudFrontEvent event)", uut.invokedMethod);
    }

    // C7: False-positive prevention — SQS body containing S3-like strings must NOT be misidentified as S3
    @Test
    void should_not_misidentify_SQSEvent_as_S3Event_when_body_contains_s3_strings() {
        String sqsWithS3Body = "{\n" +
                "  \"Records\": [{\n" +
                "    \"messageId\": \"abc123\",\n" +
                "    \"receiptHandle\": \"abc\",\n" +
                "    \"body\": \"{\\\"source\\\": \\\"aws:s3\\\", \\\"eventSource\\\": \\\"aws:s3\\\", \\\"s3SchemaVersion\\\": \\\"1.0\\\", \\\"bucket\\\": {}, \\\"object\\\": {}}\",\n" +
                "    \"attributes\": {},\n" +
                "    \"messageAttributes\": {},\n" +
                "    \"md5OfBody\": \"e4e68fb7bd0e697a0ae8f1bb342846b3\",\n" +
                "    \"eventSource\": \"aws:sqs\",\n" +
                "    \"eventSourceARN\": \"arn:aws:sqs:us-east-1:123456789012:MyQueue\",\n" +
                "    \"awsRegion\": \"us-east-1\"\n" +
                "  }]\n" +
                "}";
        InputStream sqsWithS3BodyStream = new ByteArrayInputStream(sqsWithS3Body.getBytes(StandardCharsets.UTF_8));
        uut.handleRequest(sqsWithS3BodyStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(SQSEvent event)", uut.invokedMethod);
    }

    // C8: UUT overrides handleUnknown to set invokedMethod; verify it can be called directly
    @Test
    void should_invoke_handleUnknown_when_handleUnknown_is_called_directly() {
        uut.handleUnknown(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertEquals("handleUnknown", uut.invokedMethod);
    }

    // C9: Invalid JSON causes UnknownInputStreamSourceException from StreamInput (parse failure => empty map => no match)
    @Test
    void should_throw_UnknownInputStreamSourceException_when_input_is_not_valid_json() {
        InputStream invalidJson = new ByteArrayInputStream("not json at all".getBytes(StandardCharsets.UTF_8));
        Assertions.assertThrows(UnknownInputStreamSourceException.class,
                () -> new AbstractRequestStreamHandler() {}.handleRequest(invalidJson, new ByteArrayOutputStream(), null));
    }

    // C10: Deserialization correctness — SQS
    @Test
    void should_deserialize_SQSEvent_with_correct_record_count() throws Exception {
        InputStream sqsStream = Objects.requireNonNull(
                AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/sqs-event.json"),
                "sqs-event.json cannot be null");
        AtomicReference<SQSEvent> captured = new AtomicReference<>();
        AbstractRequestStreamHandler capturer = new AbstractRequestStreamHandler() {
            @Override
            public void handle(SQSEvent event) {
                captured.set(event);
            }
        };
        capturer.handleRequest(sqsStream, new ByteArrayOutputStream(), null);
        Assertions.assertNotNull(captured.get());
        Assertions.assertNotNull(captured.get().getRecords());
        Assertions.assertFalse(captured.get().getRecords().isEmpty());
    }

    // C10: Deserialization correctness — DynamoDB
    @Test
    void should_deserialize_DynamodbEvent_with_correct_record_count() throws Exception {
        InputStream ddbStream = Objects.requireNonNull(
                AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/dynamodb.json"),
                "dynamodb.json cannot be null");
        AtomicReference<DynamodbEvent> captured = new AtomicReference<>();
        AbstractRequestStreamHandler capturer = new AbstractRequestStreamHandler() {
            @Override
            public void handle(DynamodbEvent event) {
                captured.set(event);
            }
        };
        capturer.handleRequest(ddbStream, new ByteArrayOutputStream(), null);
        Assertions.assertNotNull(captured.get());
        Assertions.assertNotNull(captured.get().getRecords());
        Assertions.assertFalse(captured.get().getRecords().isEmpty());
    }

    // C10: Deserialization correctness — API Gateway v2
    @Test
    void should_deserialize_APIGatewayV2HTTPEvent_with_non_null_requestContext() throws Exception {
        InputStream apigwStream = Objects.requireNonNull(
                AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/apigateway-aws-proxy.json"),
                "apigateway-aws-proxy.json cannot be null");
        AtomicReference<APIGatewayV2HTTPEvent> captured = new AtomicReference<>();
        AbstractRequestStreamHandler capturer = new AbstractRequestStreamHandler() {
            @Override
            public void handle(APIGatewayV2HTTPEvent event) {
                captured.set(event);
            }
        };
        capturer.handleRequest(apigwStream, new ByteArrayOutputStream(), null);
        Assertions.assertNotNull(captured.get());
        Assertions.assertNotNull(captured.get().getRequestContext());
    }

}
