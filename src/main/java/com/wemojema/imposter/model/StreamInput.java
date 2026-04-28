package com.wemojema.imposter.model;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.type.TypeReference;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.amazonaws.services.lambda.runtime.events.*;
import com.wemojema.imposter.api.UnknownInputStreamSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamInput {
    private static final Logger logger = LoggerFactory.getLogger(StreamInput.class);
    private final String json;
    private Map<String, Object> parsedJson;
    private Class<?> eventType;

    /**
     * Reads and identifies the event source of the given InputStream.
     * The stream is fully read into memory; use {@link #asInputStream()} to re-read it.
     *
     * @param input the raw Lambda event payload
     * @throws UnknownInputStreamSourceException if the event source cannot be determined
     */
    public StreamInput(InputStream input) {
        json = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        logger.trace("Received InputStream:\n" + json);
        parseJson();
        identify();
    }

    private void parseJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            parsedJson = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse InputStream as JSON", e);
            parsedJson = java.util.Collections.emptyMap();
        }
    }

    /**
     * Returns the original payload as a fresh InputStream for downstream use.
     */
    public InputStream asInputStream() {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns the identified event type class (e.g., {@code SQSEvent.class}).
     */
    public Class<?> identifiesAs() {
        return eventType;
    }

    /**
     * Deserializes the payload as an {@link APIGatewayV2HTTPEvent} (API Gateway HTTP v2).
     */
    public APIGatewayV2HTTPEvent asApiGWEvent() {
        return map(APIGatewayV2HTTPEvent.class);
    }

    /**
     * Deserializes the payload as an {@link SQSEvent}.
     */
    public SQSEvent asSQSEvent() {
        return map(SQSEvent.class);
    }

    /**
     * Deserializes the payload as a {@link DynamodbEvent}.
     */
    public DynamodbEvent asDynamodbEvent() {
        return map(DynamodbEvent.class);
    }

    /**
     * Deserializes the payload as an {@link ApplicationLoadBalancerRequestEvent}.
     */
    public ApplicationLoadBalancerRequestEvent asALBRequestEvent() {
        return map(ApplicationLoadBalancerRequestEvent.class);
    }

    /**
     * Deserializes the payload as an {@link S3Event}.
     */
    public S3Event asS3Event() {
        return map(S3Event.class);
    }

    /**
     * Deserializes the payload as a {@link CloudFrontEvent}.
     */
    public CloudFrontEvent asCloudFrontEvent() {
        return map(CloudFrontEvent.class);
    }

    /**
     * Deserializes the payload as an {@link SNSEvent}.
     */
    public SNSEvent asSNSEvent() {
        return map(SNSEvent.class);
    }

    /**
     * Deserializes the payload as a {@link ScheduledEvent} (EventBridge / scheduled rule).
     */
    public ScheduledEvent asScheduledEvent() {
        return map(ScheduledEvent.class);
    }

    /**
     * Deserializes the payload as a {@link KinesisEvent}.
     */
    public KinesisEvent asKinesisEvent() {
        return map(KinesisEvent.class);
    }

    /**
     * Deserializes the payload as an {@link APIGatewayProxyRequestEvent} (API Gateway REST v1).
     */
    public APIGatewayProxyRequestEvent asAPIGatewayProxyRequestEvent() {
        return map(APIGatewayProxyRequestEvent.class);
    }

    // Helper: check if the top-level parsed map has a key
    private boolean hasKey(String key) {
        return parsedJson.containsKey(key);
    }

    // Helper: check if the top-level parsed map has a key with a specific value
    private boolean hasKeyWithValue(String key, String value) {
        return value.equals(parsedJson.get(key));
    }

    // Helper: returns the first element of the "Records" array, or null if not present/empty
    @SuppressWarnings("unchecked")
    private Map<String, Object> firstRecord() {
        Object records = parsedJson.get("Records");
        if (records instanceof List) {
            List<?> list = (List<?>) records;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                return (Map<String, Object>) list.get(0);
            }
        }
        return null;
    }

    // Helper: check if the first record has a key with a specific value (case-sensitive)
    private boolean firstRecordHasKeyWithValue(String key, String value) {
        Map<String, Object> record = firstRecord();
        return record != null && value.equals(record.get(key));
    }

    // Helper: check if the first record has a specific key
    private boolean firstRecordHasKey(String key) {
        Map<String, Object> record = firstRecord();
        return record != null && record.containsKey(key);
    }

    private void identify() {
        if (identifiesAsCognitoPreTokenGenerationEvent()) {
            this.eventType = CognitoUserPoolPreTokenGenerationEvent.class;
            return;
        }
        if (identifiesAsAPIGatewayV2HTTPEvent()) {
            this.eventType = APIGatewayV2HTTPEvent.class;
            return;
        }
        if (identifiesAsALBRequestEvent()) {
            this.eventType = ApplicationLoadBalancerRequestEvent.class;
            return;
        }
        if (identifiesAsSQSEvent()) {
            this.eventType = SQSEvent.class;
            return;
        }
        if (identifiesAsDDBEvent()) {
            this.eventType = DynamodbEvent.class;
            return;
        }
        if (identifiesAsS3Event()) {
            this.eventType = S3Event.class;
            return;
        }
        if (identifiesAsCloudFrontEvent()) {
            this.eventType = CloudFrontEvent.class;
            return;
        }
        if (identifiesAsSNSEvent()) {
            this.eventType = SNSEvent.class;
            return;
        }
        if (identifiesAsScheduledEvent()) {
            this.eventType = ScheduledEvent.class;
            return;
        }
        if (identifiesAsKinesisEvent()) {
            this.eventType = KinesisEvent.class;
            return;
        }
        if (identifiesAsHTTPAPIGWEvent()) {
            this.eventType = APIGatewayProxyRequestEvent.class;
            return;
        }

        logger.error("Unable to identify this input stream:\n" + this.json);
        throw new UnknownInputStreamSourceException("InputStream must be one of the following Types: [" +
                "APIGatewayV2HTTPEvent, " +
                "APIGatewayProxyRequestEvent, " +
                "ApplicationLoadBalancerRequestEvent, " +
                "CloudFrontEvent, " +
                "CognitoUserPoolPreTokenGenerationEvent, " +
                "DynamodbEvent, " +
                "KinesisEvent, " +
                "S3Event, " +
                "ScheduledEvent, " +
                "SNSEvent, " +
                "SQSEvent" +
                "]");
    }

    /**
     * Deserializes the payload as a {@link CognitoUserPoolPreTokenGenerationEvent}.
     */
    public CognitoUserPoolPreTokenGenerationEvent asCognitoPreTokenGenerationEvent() {
        return map(CognitoUserPoolPreTokenGenerationEvent.class);
    }

    /**
     * Cognito Pre Token Generation trigger: has top-level "triggerSource" starting with "TokenGeneration".
     * Checked first as it has no overlap with HTTP or queue-based events.
     */
    private boolean identifiesAsCognitoPreTokenGenerationEvent() {
        Object triggerSource = parsedJson.get("triggerSource");
        return triggerSource instanceof String && ((String) triggerSource).startsWith("TokenGeneration");
    }

    /**
     * APIGatewayV2HTTPEvent: has "routeKey" at top level AND requestContext.http sub-object.
     * Must be checked before APIGatewayProxyRequestEvent (V1) since they share fields.
     */
    @SuppressWarnings("unchecked")
    private boolean identifiesAsAPIGatewayV2HTTPEvent() {
        if (!hasKey("routeKey")) {
            return false;
        }
        Object requestContext = parsedJson.get("requestContext");
        if (!(requestContext instanceof Map)) {
            return false;
        }
        Map<String, Object> ctx = (Map<String, Object>) requestContext;
        return ctx.containsKey("http");
    }

    /**
     * ALB event: requestContext.elb exists, and httpMethod at top level.
     */
    @SuppressWarnings("unchecked")
    private boolean identifiesAsALBRequestEvent() {
        Object requestContext = parsedJson.get("requestContext");
        if (!(requestContext instanceof Map)) {
            return false;
        }
        Map<String, Object> ctx = (Map<String, Object>) requestContext;
        return ctx.containsKey("elb") && hasKey("httpMethod");
    }

    /**
     * SQS event: Records array, first record eventSource = "aws:sqs".
     */
    private boolean identifiesAsSQSEvent() {
        return hasKey("Records") && firstRecordHasKeyWithValue("eventSource", "aws:sqs");
    }

    /**
     * DynamoDB Streams event: Records array, first record eventSource = "aws:dynamodb".
     */
    private boolean identifiesAsDDBEvent() {
        return hasKey("Records") && firstRecordHasKeyWithValue("eventSource", "aws:dynamodb");
    }

    /**
     * S3 event: Records array, first record eventSource = "aws:s3" and has "s3" key.
     */
    private boolean identifiesAsS3Event() {
        return hasKey("Records")
                && firstRecordHasKeyWithValue("eventSource", "aws:s3")
                && firstRecordHasKey("s3");
    }

    /**
     * CloudFront event: Records array, first record has "cf" key (CloudFront viewer request/response).
     */
    private boolean identifiesAsCloudFrontEvent() {
        return hasKey("Records") && firstRecordHasKey("cf");
    }

    /**
     * SNS event: Records array, first record EventSource = "aws:sns" (note capital E in EventSource).
     */
    private boolean identifiesAsSNSEvent() {
        return hasKey("Records") && firstRecordHasKeyWithValue("EventSource", "aws:sns");
    }

    /**
     * Kinesis event: Records array, first record eventSource = "aws:kinesis".
     */
    private boolean identifiesAsKinesisEvent() {
        return hasKey("Records") && firstRecordHasKeyWithValue("eventSource", "aws:kinesis");
    }

    /**
     * EventBridge / ScheduledEvent: has top-level "source", "detail-type", and "detail" keys.
     */
    private boolean identifiesAsScheduledEvent() {
        return hasKey("source") && hasKey("detail-type") && hasKey("detail");
    }

    /**
     * APIGatewayProxyRequestEvent (V1): has requestContext, resource, path, headers, multiValueHeaders, body.
     * Must be checked LAST among HTTP events since V2 must be checked first.
     */
    private boolean identifiesAsHTTPAPIGWEvent() {
        return hasKey("requestContext")
                && hasKey("resource")
                && hasKey("path")
                && hasKey("headers")
                && hasKey("multiValueHeaders");
    }

    private <T> T map(Class<T> clazz) {
        try {
            PojoSerializer<T> serializer = LambdaEventSerializers.serializerFor(clazz, clazz.getClassLoader());
            return serializer.fromJson(json);
        } catch (Exception e) {
            logger.error("Failed to Cast InputStream to " + clazz.getName() +
                    " enable TRACE logging to see the String value of the InputStream", e);
            throw new UnknownInputStreamSourceException("Could not properly cast InputStream as: " + clazz.getSimpleName(), e);
        }
    }
}
