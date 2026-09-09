package com.grammar.app.grammar_backend.entity.lesson_generation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonCommonMistakes(@NotBlank String title,
                @NotNull @Size(min = 3, max = 3) List<@Valid CommonMistake> commonMistakes) {

}
