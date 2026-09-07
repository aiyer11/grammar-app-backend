package com.grammar.app.grammar_backend.entity.lesson_generation;

import java.util.List;

public record LessonContent(
        String title,
        String goal,
        String summary,
        List<LessonSection> sections,
        List<LessonExercise> exercises) {
}
