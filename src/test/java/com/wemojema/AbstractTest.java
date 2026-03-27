package com.wemojema;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class AbstractTest {

    ObjectMapper objectMapper = new ObjectMapper();

    protected InputStream inputStreamOf(Object object) throws JsonProcessingException {
        return new ByteArrayInputStream(objectMapper.writeValueAsString(object)
                .getBytes(StandardCharsets.UTF_8));
    }

}
