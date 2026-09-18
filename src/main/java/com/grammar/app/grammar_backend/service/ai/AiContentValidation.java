package com.grammar.app.grammar_backend.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonQualityReview;
import com.grammar.app.grammar_backend.exceptions.AiResponseParsingException;

@Component
public class AiContentValidation {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiResponseExecutor executor;

    public AiContentValidation(
            AiResponseExecutor executor,
            @Qualifier("deepSeekChatClient") ChatClient chatClient) {
        this.executor = executor;
        this.chatClient = chatClient;
    }

    public LessonQualityReview outputQualityReview(LessonContent lessonContent) {
        try {
            String lessonJson = objectMapper.writeValueAsString(lessonContent);

            String prompt = """
                    You are a meticulous English grammar reviewer.

                    Review this lesson for:
                    - factual grammar errors
                    - incorrect classifications
                    - misleading explanations
                    - incorrect exercise answers
                    - explanations that contradict the answer
                    - duplicate or irrelevant exercises
                    - content inappropriate for the difficulty level

                    Approve only when the lesson is factually correct.

                    Return one JSON object only.
                    Do not use Markdown fences or additional text.

                    Schema:
                    {
                      "approved": true,
                      "issues": [],
                      "suggestions": []
                    }

                    Lesson:
                    %s
                    """.formatted(lessonJson);

            return executor.execute(chatClient, prompt, LessonQualityReview.class, 1);
        } catch (JsonProcessingException exception) {
            throw new AiResponseParsingException(
                    "Could not serialize lesson for AI validation",
                    exception);
        }
    }
}
