package com.grammar.app.grammar_backend.entity.lesson_generation;

import jakarta.validation.constraints.NotBlank;

public record LessonExplanation(
                @NotBlank String title,
                @NotBlank String explanation) {

}
