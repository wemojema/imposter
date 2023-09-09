package com.wemojema.imposter.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.*;
import com.wemojema.imposter.model.StreamInput;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractRequestStreamHandler implements RequestStreamHandler {
    protected OutputStream outputStream;
    protected Context context;
    protected InputStream inputStream;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        this.outputStream = output;
        this.context = context;
        StreamInput streamInput = new StreamInput(input);
        this.inputStream = streamInput.asInputStream();
        switch (streamInput.identifiesAs().getSimpleName()) {
            case "APIGatewayProxyRequestEvent":
                handle(streamInput.asAPIGatewayProxyRequestEvent());
                break;
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

    public void handle(APIGatewayProxyRequestEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    public void handle(APIGatewayV2HTTPEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    public void handle(SQSEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    public void handle(DynamodbEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    public void handle(ApplicationLoadBalancerRequestEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    public void handle(S3Event event) {
        throwMissingHandlerException(event.getClass());
    }

    private <T> void throwMissingHandlerException(Class<T> clazz) {
        throw new MissingHandlerException("The Handler for the " + clazz.getSimpleName() +
                " has not been Overridden. \n " +
                "If you wish to handle this type of input you must override the " +
                "handle(" + clazz.getSimpleName() + " event) method.");
    }
}
