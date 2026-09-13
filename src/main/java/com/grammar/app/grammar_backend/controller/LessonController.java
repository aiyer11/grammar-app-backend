package com.grammar.app.grammar_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.service.LessonService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/getLesson")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Lesson> getLessonById(@RequestParam Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(lesson);
    }

    @PostMapping("/deleteLesson")
    @PreAuthorize("isAuthenticated()")
    public HttpStatus deleteLessonById(@RequestParam Long id) {
        lessonService.deleteLesson(id);
        return HttpStatus.OK;
    }

}
