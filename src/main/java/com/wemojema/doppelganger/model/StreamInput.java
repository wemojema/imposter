package com.wemojema.doppelganger.model;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wemojema.doppelganger.api.UnknownInputStreamSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class StreamInput {
    private static final Logger logger = LoggerFactory.getLogger(StreamInput.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String json;
    private Type type;

    public StreamInput(InputStream input) {
        json = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        logger.trace("String value of InputStream:\n" + json);
        identify();
    }

    public Class<?> identifiesAs() {
        try {
            return Class.forName(type.getTypeName());
        } catch (ClassNotFoundException e) {
            logger.error("streamInput.identifiesAs() threw ClassNotFoundException.", e);
            throw new UnknownInputStreamSourceException("Failed to identify the input provided as a known event type.");
        }
    }

    public APIGatewayV2HTTPEvent asApiGWEvent() {
        return map(APIGatewayV2HTTPEvent.class);
    }

    public SQSEvent asSQSEvent() {
        return map(SQSEvent.class);
    }

    public DynamodbEvent asDynamodbEvent() {
        return map(DynamodbEvent.class);
    }

    public ApplicationLoadBalancerRequestEvent asALBRequestEvent() {
        return map(ApplicationLoadBalancerRequestEvent.class);
    }

    public S3Event asS3Event() {
        return map(S3Event.class);
    }

    private void identify() {
        if(identifiesAsAPIGatewayV2HTTPEvent())
            this.type = APIGatewayV2HTTPEvent.class;
        if (identifiesAsALBRequestEvent())
            this.type = ApplicationLoadBalancerRequestEvent.class;

            // todo identify other input streams here - maybe

        else
            throw new UnknownInputStreamSourceException("InputStream must be one of the following Types: [" +
                    "APIGatewayV2HTTPEvent, " +
                    "ApplicationLoadBalancerRequestEvent, " +
                    "DynamodbEvent, " +
                    "S3Event, " +
                    "SQSEvent" +
                    "]");
    }

    private boolean identifiesAsAPIGatewayV2HTTPEvent() {
        return false;
    }

    private boolean identifiesAsALBRequestEvent() {
        return json.contains("\"elb\"") &&
                json.contains("\"targetGroupArn\"") &&
                json.contains("\"httpMethod\"");
    }

    private <T> T map(Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Failed to Cast InputStream to " + clazz.getName() +
                    " enable TRACE logging to see the String value of the InputStream", e);
            throw new UnknownInputStreamSourceException("Could not properly cast InputStream as: " + clazz.getSimpleName(), e);
        }
    }
}
