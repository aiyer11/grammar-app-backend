package com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section;

import jakarta.validation.constraints.NotBlank;

public record LessonExplanation(
  @NotBlank String title,
  @NotBlank String explanation
) implements LessonSection {
  @Override
  public LessonSectionType type() {
    return LessonSectionType.EXPLANATION;
  }

  @Override
  public String content() {
    return explanation;
  }
}
