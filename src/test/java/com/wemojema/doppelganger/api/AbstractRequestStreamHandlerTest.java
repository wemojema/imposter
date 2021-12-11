package com.wemojema.doppelganger.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
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
            ,"sample json InputStream cannot be null"
    );

    class UUT extends AbstractRequestStreamHandler {
        String invokedMethod;

        @Override
        void handle(APIGatewayV2HTTPEvent event) {
            this.invokedMethod = "handle(APIGatewayV2HTTPEvent event)";
        }

        @Override
        void handle(SQSEvent event) {
            this.invokedMethod = "handle(SQSEvent event)";
        }
    }

    UUT uut;

    public static class Pojo {
        @JsonProperty
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

}