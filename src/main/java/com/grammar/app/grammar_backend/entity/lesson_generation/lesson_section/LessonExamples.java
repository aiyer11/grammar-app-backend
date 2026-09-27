package com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LessonExamples(
  @NotBlank String title,
  @NotNull @Size(min = 3, max = 3) List<@NotBlank String> examples
) implements LessonSection {
  @Override
  public LessonSectionType type() {
    return LessonSectionType.EXAMPLES;
  }

  @Override
  public String content() {
    return String.join("\n", examples);
  }
}
