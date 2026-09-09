package com.grammar.app.grammar_backend.entity.lesson_generation;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonExercise(
        @NotBlank String id,
        @NotNull ExerciseType type,
        @NotBlank String objective,
        @NotBlank String prompt,
        @NotBlank String questionText,
        @NotNull @Size(min = 2) List<@NotBlank String> options,
        @NotBlank String correctAnswer,
        @NotBlank String explanation) {
}

enum ExerciseType {
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK,
    TRUE_FALSE,
    DRAG_AND_DROP,
    SENTENCE_ORDERING,
}
