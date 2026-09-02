package com.grammar.app.grammar_backend.entity;

import java.util.List;

public record LessonContent(
        String objective,
        String summary,
        int estimatedDurationMinutes,
        List<LessonSection> sections,
        List<LessonExercise> exercises) {
}
