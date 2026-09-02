package com.grammar.app.grammar_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grammar.app.grammar_backend.entity.DifficultyLevel;
import com.grammar.app.grammar_backend.entity.LessonContent;

@Service
public class LessonGenerationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(LessonGenerationService.class);

    public LessonGenerationService(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    public LessonContent generateLesson(String title, DifficultyLevel difficultyLevel)
            throws JsonMappingException, JsonProcessingException {
        String json = chatClient.prompt()
                .system("""
                        You are a grammar teacher that generates lesson content in JSON format.
                        Return only valid JSON.
                        No markdown fences.
                        No explanations.
                        No trailing commas.
                        Use valid JSON syntax only.""")
                .user("""
                            Generate a grammar lesson in valid JSON only.
                            Use this exact schema:
                            {
                              "objective": "string",
                              "summary": "string",
                              "estimatedDurationMinutes": 10,
                              "sections": [
                                {
                                  "type": "CONCEPT",
                                  "title": "string",
                                  "content": "string"
                                }
                              ],
                              "exercises": [
                                {
                                  "id": "string",
                                  "type": "MULTIPLE_CHOICE",
                                  "prompt": "string",
                                  "questionText": "string",
                                  "options": ["string", "string", "string"],
                                  "correctAnswer": "string",
                                  "explanation": "string"
                                }
                              ]
                            }
                            Topic: %s
                            Difficulty: %s
                            Return only valid JSON.
                        """.formatted(title, difficultyLevel))
                .call()
                .content();
        logger.info("Generated lesson content JSON: {}", json);

        return objectMapper.readValue(json, LessonContent.class);
    }

}
