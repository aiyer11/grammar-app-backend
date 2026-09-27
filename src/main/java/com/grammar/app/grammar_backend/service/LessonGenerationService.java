package com.grammar.app.grammar_backend.service;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCode;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExercise;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonQualityReview;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonCommonMistakes;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonExamples;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonExplanation;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonSection;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonSectionType;
import com.grammar.app.grammar_backend.exceptions.LessonValidationException;
import com.grammar.app.grammar_backend.repository.LessonConceptRepository;
import com.grammar.app.grammar_backend.repository.LessonRepository;
import com.grammar.app.grammar_backend.service.ai.AiContentValidation;
import com.grammar.app.grammar_backend.service.ai.LessonContentGenerator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LessonGenerationService {

  private final Logger LOGGER = LoggerFactory.getLogger(
    LessonGenerationService.class
  );
  private final AiContentValidation validation;
  private final LessonConceptRepository lessonConceptRepository;
  private final LessonContentGenerator generator;
  private final LessonRepository lessonRepository;
  private static final int MAX_LESSON_ATTEMPTS = 2;

  public LessonGenerationService(
    LessonContentGenerator generator,
    LessonConceptRepository lessonConceptRepository,
    LessonRepository lessonRepository,
    AiContentValidation validation
  ) {
    this.generator = generator;
    this.lessonConceptRepository = lessonConceptRepository;
    this.lessonRepository = lessonRepository;
    this.validation = validation;
  }

  private List<LessonSection> createLessonSections(
    LessonExplanation explanation,
    LessonExamples examples,
    LessonCommonMistakes commonMistakes
  ) {
    return List.of(explanation, examples, commonMistakes);
  }

  private LessonContent buildLessonContent(
    LessonConcept concept,
    List<String> previousIssues
  ) {
    LessonExplanation explanation = generator.generateExplanation(
      concept,
      previousIssues
    );
    LessonExamples examples = generator.generateExamples(
      concept,
      previousIssues
    );
    LessonCommonMistakes commonMistakes = generator.generateCommonMistakes(
      concept,
      previousIssues
    );
    List<LessonExercise> exercises = generator.generateExercises(
      concept,
      previousIssues
    );

    List<LessonSection> sections = createLessonSections(
      explanation,
      examples,
      commonMistakes
    );

    LessonContent lessonContent = new LessonContent(
      concept.getName(),
      concept.getGoal(),
      explanation.explanation(),
      sections,
      exercises
    );

    LOGGER.info("Generated lesson content for concept: {}", concept.getName());
    LOGGER.debug("Lesson content: {}", lessonContent);

    return lessonContent;
  }

  private LessonConcept getLessonConcept(String lessonCode) {
    return lessonConceptRepository
      .findByLessonCode(lessonCode)
      .orElseThrow(() ->
        new RuntimeException("Lesson concept not found for code: " + lessonCode)
      );
  }

  private LessonQualityReview validateLessonContent(
    LessonContent lessonContent
  ) {
    return validation.outputQualityReview(lessonContent);
  }

  private int estimateDuration(LessonContent lessonContent) {
    int duration = 5;

    if (lessonContent.sections() != null) {
      duration += lessonContent
        .sections()
        .stream()
        .mapToInt(section ->
          switch (section.type()) {
            case LessonSectionType.EXPLANATION -> 8;
            case LessonSectionType.EXAMPLES -> 5;
            case LessonSectionType.COMMON_MISTAKES -> 4;
            default -> 3;
          }
        )
        .sum();
    }

    if (lessonContent.exercises() != null) {
      duration += lessonContent.exercises().size() * 3;
    }

    return duration;
  }

  public Lesson generateAndSaveLesson(LessonCode lessonCode) {
    LessonConcept lessonConcept = getLessonConcept(lessonCode.name());
    LessonContent lessonContent = null;
    LessonQualityReview lessonQualityReview = null;
    List<String> previousIssues = List.of();

    for (int attempt = 1; attempt <= MAX_LESSON_ATTEMPTS; attempt++) {
      lessonContent = buildLessonContent(lessonConcept, previousIssues);
      lessonQualityReview = validateLessonContent(lessonContent);

      if (lessonQualityReview.approved()) {
        Lesson lesson = Lesson.builder()
          .title(lessonContent.title())
          .description(lessonContent.summary())
          .estimatedTime(estimateDuration(lessonContent))
          .difficultyLevel(lessonConcept.getDifficultyLevel())
          .content(lessonContent)
          .build();
        return lessonRepository.save(lesson);
      }

      previousIssues = lessonQualityReview.issues();

      LOGGER.warn(
        "Generated lesson failed quality validation on attempt {}/{}. Issues: {}",
        attempt,
        MAX_LESSON_ATTEMPTS,
        lessonQualityReview.issues()
      );
    }

    throw new LessonValidationException(
      "Lesson failed quality validation after " +
        MAX_LESSON_ATTEMPTS +
        " attempts: " +
        lessonQualityReview.issues()
    );
  }
}
