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
    private final Logger logger = LoggerFactory.getLogger(AiResponseExecutor.class);

    public <T> T execute(
            ChatClient chatClient,
            String systemPrompt,
            String userPrompt,
            Class<T> responseType) {
            try {
                return chatClient.prompt()
                        .system(systemPrompt)
                        .user(userPrompt)
                        .call()
                        .entity(responseType, ChatClient.EntityParamSpec::validateSchema);
            } catch (Exception exception) {
                logger.error("Error executing AI response: {}", exception.getMessage(), exception);
                throw new AiResponseParsingException("Failed to parse AI response", exception);
            }
    }
}
