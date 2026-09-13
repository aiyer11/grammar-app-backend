package com.grammar.app.grammar_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCode;
import com.grammar.app.grammar_backend.service.LessonGenerationService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/lessonGeneration")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class LessonGenerationController {

    private final LessonGenerationService lessonGenerationService;

    public LessonGenerationController(LessonGenerationService lessonGenerationService) {
        this.lessonGenerationService = lessonGenerationService;
    }

    @GetMapping("/generateLesson")
    @Parameter(name = "lessonCode", schema = @Schema(implementation = LessonCode.class))
    public ResponseEntity<Lesson> generateAndSaveLesson(LessonCode lessonCode) {
        Lesson lesson = lessonGenerationService.generateAndSaveLesson(lessonCode);
        return ResponseEntity.ok(lesson);
    }

}
