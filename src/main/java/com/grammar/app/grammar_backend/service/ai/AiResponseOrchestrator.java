package com.grammar.app.grammar_backend.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grammar.app.grammar_backend.exceptions.AiResponseParsingException;
import com.grammar.app.grammar_backend.util.PromptValidator;

@Component
public class AiResponseOrchestrator {

    private static final int MAX_RETRIES = 2;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatClient chatClient;
    private final PromptValidator promptValidator;
    private final Logger LOGGER = LoggerFactory.getLogger(AiResponseOrchestrator.class);

    public AiResponseOrchestrator(ChatClient.Builder chatClient, PromptValidator promptValidator) {
        this.chatClient = chatClient.build();
        this.promptValidator = promptValidator;
    }

    public <T> T execute(String prompt, Class<T> responseType) {
        Exception lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String rawResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                LOGGER.debug("Raw response: {}", rawResponse);

                T response = objectMapper.readValue(rawResponse, responseType);
                promptValidator.validate(response);

                return response;
            } catch (Exception exception) {
                lastException = exception;
                if (attempt < MAX_RETRIES) {
                    LOGGER.warn(
                            "Invalid {} response on attempt {}/{}",
                            responseType.getSimpleName(),
                            attempt + 1,
                            MAX_RETRIES + 1);
                }
            }

        }
        throw new AiResponseParsingException("Failed to generate valid " + responseType.getSimpleName(), lastException);
    }

}
