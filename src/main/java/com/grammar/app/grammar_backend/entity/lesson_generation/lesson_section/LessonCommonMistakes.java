package com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

public record LessonCommonMistakes(
  @NotBlank String title,
  @NotNull @Size(min = 3, max = 3) List<@Valid CommonMistake> commonMistakes
) implements LessonSection {
  @Override
  public LessonSectionType type() {
    return LessonSectionType.COMMON_MISTAKES;
  }

  @Override
  public String content() {
    return commonMistakes
      .stream()
      .map(
        item ->
          item.mistake() + " — " + item.explanation() + " Fix: " + item.fix()
      )
      .collect(Collectors.joining("\n"));
  }
}
