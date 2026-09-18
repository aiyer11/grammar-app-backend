package com.grammar.app.grammar_backend.entity.lesson_generation;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public record LessonQualityReview(boolean approved, @NotNull List<String> issues, @NotNull List<String> suggestions) {

}
