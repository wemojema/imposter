package com.wemojema.doppelganger.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;
import java.util.Map;

public class AwsEvent {

    @JsonAlias("Records")
    private List<Map<String, Object>> records;

}
