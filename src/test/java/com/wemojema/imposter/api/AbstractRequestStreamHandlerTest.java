package com.wemojema.imposter.api;

import com.amazonaws.services.lambda.runtime.events.*;
import com.wemojema.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

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
    InputStream s3InputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/s3event.json")
            , "sample json InputStream cannot be null"
    );
    InputStream albInputStream = Objects.requireNonNull(
            AbstractRequestStreamHandlerTest.class.getClassLoader().getResourceAsStream("sample-json/alb-event.json")
            , "sample json InputStream cannot be null"
    );


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

    @Test
    void should_throw_UnknownInputStreamException_when_it_cannot_determine_the_InputStream_source() {
        Assertions.assertThrows(UnknownInputStreamSourceException.class,
                () -> uut.handleRequest(inputStreamOf(new Pojo("testing")), new ByteArrayOutputStream(), null));
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

//    @Test
//    void should_identify_a_S3EventNotification_when_provided_a_valid_payload_for_such_an_event() {
//        uut.handleRequest(s3InputStream, new ByteArrayOutputStream(), null);
//        // todo
////        Assertions.assertEquals("handle(S3Event event)", uut.invokedMethod);
//    }

    @Test
    void should_identify_an_ApplicationLoadBalancerEvent_when_provided_a_valid_payload_for_such_an_event() {
        uut.handleRequest(albInputStream, new ByteArrayOutputStream(), null);
        Assertions.assertEquals("handle(ApplicationLoadBalancerRequestEvent event)", uut.invokedMethod);
    }

}