package com.wemojema.doppelganger.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.wemojema.doppelganger.model.StreamInput;

import java.io.InputStream;
import java.io.OutputStream;

public class AbstractRequestStreamHandler implements RequestStreamHandler {
    protected OutputStream outputStream;
    protected Context context;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        this.outputStream = output;
        this.context = context;
        StreamInput streamInput = new StreamInput(input);
        switch (streamInput.identifiesAs().getSimpleName()) {
            case "APIGatewayV2HTTPEvent":
                handle(streamInput.asApiGWEvent());
                break;
            case "SQSEvent":
                handle(streamInput.asSQSEvent());
                break;
            case "DynamodbEvent":
                handle(streamInput.asDynamodbEvent());
                break;
            case "ApplicationLoadBalancerRequestEvent":
                handle(streamInput.asALBRequestEvent());
                break;
            case "S3Event":
                handle(streamInput.asS3Event());
                break;
        }
    }

    void handle(APIGatewayV2HTTPEvent event) {
        throwMissingHandlerException(APIGatewayV2HTTPEvent.class);
    }

    void handle(SQSEvent event) {
        throwMissingHandlerException(SQSEvent.class);
    }

    void handle(DynamodbEvent event) {
        throwMissingHandlerException(DynamodbEvent.class);
    }

    void handle(ApplicationLoadBalancerRequestEvent event) {
        throwMissingHandlerException(ApplicationLoadBalancerRequestEvent.class);
    }

    void handle(S3Event event) {
        throwMissingHandlerException(S3Event.class);
    }

    private <T> void throwMissingHandlerException(Class<T> clazz) {
        throw new MissingHandlerException("The Handler for the " + clazz.getSimpleName() +
                " has not been Overridden. \n " +
                "If you wish to handle this type of input you must override the " +
                "handle(" + clazz.getSimpleName() + " event) method.");
    }
}
