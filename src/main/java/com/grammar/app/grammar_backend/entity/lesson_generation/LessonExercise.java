package com.grammar.app.grammar_backend.entity.lesson_generation;

import java.util.List;

public record LessonExercise(
        String id,
        ExerciseType type,
        String objective,
        String prompt,
        String questionText,
        List<String> options,
        String correctAnswer,
        String explanation) {
}

enum ExerciseType {
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK,
    TRUE_FALSE,
    DRAG_AND_DROP,
    SENTENCE_ORDERING,
}
