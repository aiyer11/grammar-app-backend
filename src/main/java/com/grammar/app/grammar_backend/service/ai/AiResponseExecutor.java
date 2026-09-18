package com.grammar.app.grammar_backend.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grammar.app.grammar_backend.exceptions.AiResponseParsingException;
import com.grammar.app.grammar_backend.util.PromptValidator;

@Component
public class AiResponseExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PromptValidator promptValidator;
    private final Logger logger = LoggerFactory.getLogger(AiResponseExecutor.class);

    public AiResponseExecutor(
            PromptValidator promptValidator) {
        this.promptValidator = promptValidator;
    }

    public <T> T execute(
            ChatClient chatClient,
            String prompt,
            Class<T> responseType,
            int maxRetries) {

        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String rawResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                T response = objectMapper.readValue(rawResponse, responseType);
                promptValidator.validate(response);

                return response;
            } catch (Exception exception) {
                lastException = exception;

                if (attempt < maxRetries) {
                    logger.warn(
                            "Invalid {} response, retrying ({}/{})",
                            responseType.getSimpleName(),
                            attempt + 1,
                            maxRetries);
                }
            }
        }

        throw new AiResponseParsingException(
                "Failed to generate valid " + responseType.getSimpleName(),
                lastException);
    }
}
