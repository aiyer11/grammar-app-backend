package com.grammar.app.grammar_backend.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.grammar.app.grammar_backend.entity.DifficultyLevel;
import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.entity.LessonContent;
import com.grammar.app.grammar_backend.repository.LessonRepository;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonGenerationService lessonGenerationService;

    public LessonService(LessonRepository lessonRepository, LessonGenerationService lessonGenerationService) {
        this.lessonRepository = lessonRepository;
        this.lessonGenerationService = lessonGenerationService;
    }

    public Lesson generateAndSaveLesson(String title, DifficultyLevel difficultyLevel)
            throws JsonMappingException, JsonProcessingException {
        LessonContent lessonContent = lessonGenerationService.generateLesson(title, difficultyLevel);
        Lesson lesson = Lesson.builder()
                .title(title)
                .description(lessonContent.summary())
                .difficultyLevel(difficultyLevel)
                .durationInMinutes(lessonContent.estimatedDurationMinutes())
                .build();

        return lessonRepository.save(lesson);
    }

    public Lesson getLessonByTitle(String title) {
        return lessonRepository.findByTitle(title)
                .orElseThrow(() -> new RuntimeException("Lesson not found with title: " + title));
    }
}
