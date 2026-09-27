package com.grammar.app.grammar_backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class ObjectMapperUtility {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convertFromJson(String fileName, Class<T> valueType) {
        try {
            InputStream fileStream = ObjectMapperUtility.class.getClassLoader().getResourceAsStream(fileName);
            return objectMapper.readValue(fileStream, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON file: " + fileName, e);
        }
    }
}
