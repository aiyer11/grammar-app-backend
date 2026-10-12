package com.grammar.app.grammar_backend.entity.lesson_generation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LessonExercise(
    String id,
    @NotNull ExerciseType type,
    @NotBlank String objective,
    @NotBlank String prompt,
    @NotBlank String questionText,
    @NotNull @Size(min = 2) List<@NotBlank String> options,
    @NotBlank String correctAnswer,
    @NotBlank String explanation
) {
    public LessonExercise withId(String id) {
        return new LessonExercise(id, type, objective, prompt, questionText, options, correctAnswer, explanation);
    }
}
