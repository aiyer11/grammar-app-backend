package com.grammar.app.grammar_backend.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AiResponseOrchestrator {
    private final ChatClient chatClient;
    private final AiResponseExecutor executor;

    public AiResponseOrchestrator(@Qualifier("localChatClient") ChatClient chatClient,
            AiResponseExecutor executor) {
        this.chatClient = chatClient;
        this.executor = executor;
    }

    public <T> T execute(String prompt, Class<T> responseType) {
        return executor.execute(chatClient, prompt, responseType, 1);
    }
}
