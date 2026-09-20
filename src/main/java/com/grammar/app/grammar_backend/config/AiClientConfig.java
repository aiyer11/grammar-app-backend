package com.grammar.app.grammar_backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    @Bean("localChatClient")
    ChatClient localChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(OllamaChatOptions.builder().temperature(0.3))
                .build();
    }

    @Bean("deepSeekChatClient")
    ChatClient deepSeekChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(OpenAiChatOptions.builder().temperature(0.3))
                .build();
    }

}
