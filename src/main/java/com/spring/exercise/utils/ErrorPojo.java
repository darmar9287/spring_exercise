package com.spring.exercise.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ErrorPojo {
    @JsonProperty("field")
    private String field;
    @JsonProperty("message")
    private String message;

    // A default constructor is required for serialization/deserialization to work
    public ErrorPojo() {
    }

    public ErrorPojo(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ErrorPojo(String message) {
        this.message = message;
    }
}