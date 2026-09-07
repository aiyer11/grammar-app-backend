package com.grammar.app.grammar_backend.service;

import org.springframework.stereotype.Service;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCode;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.repository.LessonConceptRepository;
import com.grammar.app.grammar_backend.repository.LessonRepository;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonConceptRepository lessonConceptRepository;
    private final LessonGenerationService lessonGenerationService;

    public LessonService(LessonRepository lessonRepository, LessonGenerationService lessonGenerationService,
            LessonConceptRepository lessonConceptRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonConceptRepository = lessonConceptRepository;
        this.lessonGenerationService = lessonGenerationService;
    }

    private LessonConcept getLessonConcept(String lessonCode) {
        LessonConcept lessonConcept = lessonConceptRepository.findByLessonCode(lessonCode)
                .orElseThrow(() -> new RuntimeException("Lesson concept not found for code: " + lessonCode));
        return lessonConcept;
    }

    private int estimateDuration(LessonContent lessonContent) {
        int duration = 5;

        if (lessonContent.sections() != null) {
            duration += lessonContent.sections().stream()
                    .mapToInt(section -> {
                        switch (section.type().toUpperCase()) {
                            case "EXPLANATION":
                                return 8;
                            case "EXAMPLES":
                                return 5;
                            case "COMMON_MISTAKES":
                                return 4;
                            default:
                                return 3;
                        }
                    })
                    .sum();
        }

        if (lessonContent.exercises() != null) {
            duration += lessonContent.exercises().size() * 3;
        }

        return duration;
    }

    public Lesson generateAndSaveLesson(LessonCode lessonCode) {
        LessonConcept lessonConcept = getLessonConcept(lessonCode.name());
        LessonContent lessonContent = lessonGenerationService.buildLessonContent(lessonConcept);
        Lesson lesson = Lesson.builder()
                .title(lessonContent.title())
                .description(lessonContent.summary())
                .estimatedTime(estimateDuration(lessonContent))
                .difficultyLevel(lessonConcept.getDifficultyLevel())
                .content(lessonContent)
                .build();
        return lessonRepository.save(lesson);
    }

    public Lesson getLessonByTitle(String title) {
        return lessonRepository.findByTitle(title)
                .orElseThrow(() -> new RuntimeException("Lesson not found with title: " + title));
    }
}
