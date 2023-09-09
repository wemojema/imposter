package com.wemojema.imposter.model;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonGenerator;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonParser;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.*;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.module.SimpleModule;
import com.amazonaws.services.lambda.runtime.events.*;
import com.wemojema.imposter.api.UnknownInputStreamSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

public class StreamInput {
    private static final Logger logger = LoggerFactory.getLogger(StreamInput.class);
    private final String json;
    private Type type;

    public StreamInput(InputStream input) {
        json = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        logger.trace("Received InputStream:\n" + json);
        identify();
    }


    public InputStream asInputStream() {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
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
        if (identifiesAsAPIGatewayV2HTTPEvent()) {
            this.type = APIGatewayV2HTTPEvent.class;
            return;
        }
        if (identifiesAsALBRequestEvent()) {
            this.type = ApplicationLoadBalancerRequestEvent.class;
            return;
        }
        if (identifiesAsSQSEvent()) {
            this.type = SQSEvent.class;
            return;
        }
        if (identifiesAsDDBEvent()) {
            this.type = DynamodbEvent.class;
            return;
        }
        if (identifiesAsS3Event()) {
            this.type = S3Event.class;
            return;
        }
        // todo identify other input streams here
        logger.error("Unable to identify this input stream:\n" + this.json);
        throw new UnknownInputStreamSourceException("InputStream must be one of the following Types: [" +
                "APIGatewayV2HTTPEvent, " +
                "ApplicationLoadBalancerRequestEvent, " +
                "DynamodbEvent, " +
                "S3Event, " +
                "SQSEvent" +
                "]");
    }

    private boolean identifiesAsS3Event() {
        return json.replace(" ", "")
                .contains("\"eventSource\":\"aws:s3\"") &&
                json.contains("\"s3\"") &&
                json.contains("\"object\"") &&
                json.contains("\"bucket\"") &&
                json.contains("\"s3SchemaVersion\"");
    }

    private boolean identifiesAsDDBEvent() {
        return json.contains("\"aws:dynamodb\"") &&
                json.contains("\"Records\"") &&
                json.contains("\"eventName\"") &&
                json.contains("\"dynamodb\"");
    }

    private boolean identifiesAsAPIGatewayV2HTTPEvent() {
        return json.contains("\"requestContext\"") &&
                json.contains("\"http\"") &&
                json.contains("\"method\"") &&
                json.contains("\"protocol\"") &&
                json.contains("\"stage\"") &&
                json.contains("\"routeKey\"");
    }

    private boolean identifiesAsSQSEvent() {
        return json.contains("\"eventSource\"") &&
                json.contains("\"aws:sqs\"") &&
                json.contains("\"eventSourceARN\"") &&
                json.contains(":sqs:") &&
                json.contains("\"md5OfBody\"");
    }

    private boolean identifiesAsALBRequestEvent() {
        return json.contains("\"elb\"") &&
                json.contains("\"targetGroupArn\"") &&
                json.contains("\"httpMethod\"");
    }

    public static class TimestampDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(jsonParser.getValueAsLong());
            return calendar.getTime();
        }
    }

    public static class TimestampSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(date.toString());
        }
    }

    private <T> T map(Class<T> clazz) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Date.class, new TimestampSerializer());
            module.addDeserializer(Date.class, new TimestampDeserializer());
            objectMapper.registerModule(module);
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            return objectMapper
                    .readerFor(clazz)
                    .withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to Cast InputStream to " + clazz.getName() +
                    " enable TRACE logging to see the String value of the InputStream", e);
            throw new UnknownInputStreamSourceException("Could not properly cast InputStream as: " + clazz.getSimpleName(), e);
        }
    }
}
