package com.grammar.app.grammar_backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    @Bean("localChatClient")
    ChatClient localChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean("deepSeekChatClient")
    ChatClient deepSeekChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

}
