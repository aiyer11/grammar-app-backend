package com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section;

public sealed interface LessonSection
  permits LessonExplanation, LessonExamples, LessonCommonMistakes
{
  LessonSectionType type();
  String title();
  String content();
}
