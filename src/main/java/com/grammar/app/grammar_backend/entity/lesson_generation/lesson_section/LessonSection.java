package com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = LessonExplanation.class,
                name = "EXPLANATION"
        ),
        @JsonSubTypes.Type(
                value = LessonExamples.class,
                name = "EXAMPLES"
        ),
        @JsonSubTypes.Type(
                value = LessonCommonMistakes.class,
                name = "COMMON_MISTAKES"
        )
})
public sealed interface LessonSection
  permits LessonExplanation, LessonExamples, LessonCommonMistakes
{
  LessonSectionType type();
  String title();
  String content();
}
