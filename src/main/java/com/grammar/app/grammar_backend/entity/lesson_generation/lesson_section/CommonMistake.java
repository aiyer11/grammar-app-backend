package com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section;

import jakarta.validation.constraints.NotBlank;

public record CommonMistake(
  @NotBlank String mistake,
  @NotBlank String explanation,
  @NotBlank String fix
) {}
