package com.grammar.app.grammar_backend.exceptions;

public class AiResponseParsingException extends RuntimeException {
    public AiResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
