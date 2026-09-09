package com.grammar.app.grammar_backend.util;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Component
public class PromptValidator {

    private final Validator validator;

    public PromptValidator(Validator validator) {
        this.validator = validator;

    }

    public <T> void validate(T value) {
        Set<ConstraintViolation<T>> violations = validator.validate(value);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", ")));
        }
    }

}
