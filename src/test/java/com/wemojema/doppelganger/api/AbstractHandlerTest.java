package com.wemojema.doppelganger.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wemojema.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

class AbstractHandlerTest extends AbstractTest {

    class UUT extends AbstractHandler {

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
        // FAILED Expected com.wemojema.doppelganger.api.UnknownInputStreamSourceException to be thrown, but nothing was thrown.
        Assertions.assertThrows(UnknownInputStreamSourceException.class,
                () -> uut.handleRequest(inputStreamOf(new Pojo("testing")), new ByteArrayOutputStream(), null));
    }

}