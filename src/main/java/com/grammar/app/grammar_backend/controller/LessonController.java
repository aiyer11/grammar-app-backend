package com.grammar.app.grammar_backend.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCode;
import com.grammar.app.grammar_backend.service.LessonService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/generateLesson")
    @Parameter(name = "lessonCode", schema = @Schema(implementation = LessonCode.class))
    public ResponseEntity<Lesson> generateAndSaveLesson(LessonCode lessonCode) {
        Lesson lesson = lessonService.generateAndSaveLesson(lessonCode);
        return ResponseEntity.ok(lesson);
    }

    @GetMapping("/getLesson")
    @Parameter(name = "title", required = true, description = "The title of the lesson to retrieve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Lesson> getLessonByTitle(@Param("title") String title) {
        Lesson lesson = lessonService.getLessonByTitle(title);
        return ResponseEntity.ok(lesson);
    }

}
