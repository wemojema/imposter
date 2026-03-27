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

    /**
     * Routes the inbound Lambda event to the appropriate typed {@code handle()} method.
     * Parses and identifies the event source, then dispatches to the matching overload.
     * If the event source cannot be identified, {@link #handleUnknown(InputStream)} is called.
     */
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        this.outputStream = output;
        this.context = context;
        StreamInput streamInput = new StreamInput(input);
        this.inputStream = streamInput.asInputStream();
        Class<?> eventType = streamInput.identifiesAs();
        if (eventType == APIGatewayProxyRequestEvent.class) {
            handle(streamInput.asAPIGatewayProxyRequestEvent());
        } else if (eventType == APIGatewayV2HTTPEvent.class) {
            handle(streamInput.asApiGWEvent());
        } else if (eventType == SQSEvent.class) {
            handle(streamInput.asSQSEvent());
        } else if (eventType == DynamodbEvent.class) {
            handle(streamInput.asDynamodbEvent());
        } else if (eventType == ApplicationLoadBalancerRequestEvent.class) {
            handle(streamInput.asALBRequestEvent());
        } else if (eventType == S3Event.class) {
            handle(streamInput.asS3Event());
        } else if (eventType == CloudFrontEvent.class) {
            handle(streamInput.asCloudFrontEvent());
        } else if (eventType == SNSEvent.class) {
            handle(streamInput.asSNSEvent());
        } else if (eventType == ScheduledEvent.class) {
            handle(streamInput.asScheduledEvent());
        } else if (eventType == KinesisEvent.class) {
            handle(streamInput.asKinesisEvent());
        } else {
            handleUnknown(this.inputStream);
        }
    }

    /**
     * Handle an API Gateway REST (v1) proxy request event.
     * Override this method to process {@link APIGatewayProxyRequestEvent} payloads.
     */
    public void handle(APIGatewayProxyRequestEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle an API Gateway HTTP (v2) event.
     * Override this method to process {@link APIGatewayV2HTTPEvent} payloads.
     */
    public void handle(APIGatewayV2HTTPEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle an SQS event.
     * Override this method to process {@link SQSEvent} payloads.
     */
    public void handle(SQSEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle a DynamoDB Streams event.
     * Override this method to process {@link DynamodbEvent} payloads.
     */
    public void handle(DynamodbEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle an Application Load Balancer request event.
     * Override this method to process {@link ApplicationLoadBalancerRequestEvent} payloads.
     */
    public void handle(ApplicationLoadBalancerRequestEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle an S3 event.
     * Override this method to process {@link S3Event} payloads.
     */
    public void handle(S3Event event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle a CloudFront (Lambda@Edge) event.
     * Override this method to process {@link CloudFrontEvent} payloads.
     */
    public void handle(CloudFrontEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle an SNS event.
     * Override this method to process {@link SNSEvent} payloads.
     */
    public void handle(SNSEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle an EventBridge or scheduled event.
     * Override this method to process {@link ScheduledEvent} payloads.
     */
    public void handle(ScheduledEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Handle a Kinesis event.
     * Override this method to process {@link KinesisEvent} payloads.
     */
    public void handle(KinesisEvent event) {
        throwMissingHandlerException(event.getClass());
    }

    /**
     * Called when the event source cannot be identified.
     * Override to handle unknown events gracefully; by default throws {@link UnknownInputStreamSourceException}.
     *
     * @param input the raw event payload
     */
    public void handleUnknown(InputStream input) {
        throw new UnknownInputStreamSourceException("Received an unknown or unhandled input stream source.");
    }

    private <T> void throwMissingHandlerException(Class<T> clazz) {
        throw new MissingHandlerException("The Handler for the " + clazz.getSimpleName() +
                " has not been Overridden. \n " +
                "If you wish to handle this type of input you must override the " +
                "handle(" + clazz.getSimpleName() + " event) method.");
    }
}
