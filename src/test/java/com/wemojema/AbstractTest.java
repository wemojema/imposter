package com.wemojema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class AbstractTest {

    protected static Faker faker = new Faker();
    protected static ObjectMapper objectMapper = new ObjectMapper();

    protected InputStream inputStreamOf(Object object) throws JsonProcessingException {
        return new ByteArrayInputStream(objectMapper.writeValueAsString(object)
                .getBytes(StandardCharsets.UTF_8));
    }

}
