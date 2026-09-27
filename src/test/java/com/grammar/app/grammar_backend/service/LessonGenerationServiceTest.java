package com.grammar.app.grammar_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCode;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonQualityReview;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonCommonMistakes;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonExamples;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonExplanation;
import com.grammar.app.grammar_backend.exceptions.LessonValidationException;
import com.grammar.app.grammar_backend.repository.LessonConceptRepository;
import com.grammar.app.grammar_backend.repository.LessonRepository;
import com.grammar.app.grammar_backend.service.ai.AiContentValidation;
import com.grammar.app.grammar_backend.service.ai.LessonContentGenerator;
import com.grammar.app.grammar_backend.util.ObjectMapperUtility;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonGenerationServiceTest {

  @Mock
  private LessonContentGenerator lessonContentGenerator;

  @Mock
  private LessonConceptRepository lessonConceptRepository;

  @Mock
  private LessonRepository lessonRepository;

  @Mock
  private AiContentValidation aiContentValidation;

  @InjectMocks
  private LessonGenerationService lessonGenerationService;

  LessonCode lessonCode;
  LessonConcept lessonConcept;

  @BeforeEach
  void setup() {
    lessonCode = LessonCode.COMPLETE_SENTENCES_VS_FRAGMENTS;
    lessonConcept = ObjectMapperUtility.convertFromJson(
      "mockJson/LessonConcept.json",
      LessonConcept.class
    );
  }

  @Test
  void generateAndSaveLesson_whenReviewApproves_savesAndReturnsLesson() {
    LessonQualityReview approvedReview = new LessonQualityReview(
      true,
      List.of(),
      List.of()
    );
    stubGeneratedContent();
    Mockito.when(
      aiContentValidation.outputQualityReview(any(LessonContent.class))
    ).thenReturn(approvedReview);
    Mockito.when(lessonRepository.save(any(Lesson.class))).thenAnswer(
      invocation -> invocation.getArgument(0)
    );

    Lesson savedLesson = lessonGenerationService.generateAndSaveLesson(
      lessonCode
    );

    assertNotNull(savedLesson);
    assertEquals("Complete Sentences vs. Fragments", savedLesson.getTitle());
    assertEquals(
      lessonConcept.getDifficultyLevel(),
      savedLesson.getDifficultyLevel()
    );
    assertEquals(22, savedLesson.getEstimatedTime());
    assertEquals(
      "A complete sentence expresses a complete thought.",
      savedLesson.getContent().summary()
    );
    assertEquals(3, savedLesson.getContent().sections().size());

    verify(lessonRepository, times(1)).save(any(Lesson.class));
    verify(aiContentValidation, times(1)).outputQualityReview(
      any(LessonContent.class)
    );
  }

  @Test
  void generateAndSaveLesson_whenFirstReviewRejectsAndSecondApproves_savesLesson() {
    List<String> issues = List.of("Clarify the explanation");
    LessonQualityReview rejectedReview = new LessonQualityReview(
      false,
      issues,
      List.of()
    );
    LessonQualityReview approvedReview = new LessonQualityReview(
      true,
      List.of(),
      List.of()
    );
    Mockito.when(
      aiContentValidation.outputQualityReview(any(LessonContent.class))
    ).thenReturn(rejectedReview, approvedReview);
    stubGeneratedContent();
    Mockito.when(lessonRepository.save(any(Lesson.class))).thenAnswer(
      invocation -> invocation.getArgument(0)
    );
    Lesson savedLesson = lessonGenerationService.generateAndSaveLesson(
      lessonCode
    );
    assertNotNull(savedLesson);
    verify(aiContentValidation, times(2)).outputQualityReview(
      any(LessonContent.class)
    );
    verify(lessonRepository, times(1)).save(any(Lesson.class));

    verify(lessonContentGenerator).generateExplanation(lessonConcept, issues);
    verify(lessonContentGenerator).generateExamples(lessonConcept, issues);
    verify(lessonContentGenerator).generateCommonMistakes(
      lessonConcept,
      issues
    );
    verify(lessonContentGenerator).generateExercises(lessonConcept, issues);
  }

  @Test
  void generateAndSaveLesson_whenBothReviewsReject_throwsAndDoesNotSave() {
    List<String> issues = List.of("Clarify the explanation");
    LessonQualityReview rejectedReview = new LessonQualityReview(
      false,
      issues,
      List.of()
    );
    stubGeneratedContent();
    Mockito.when(
      aiContentValidation.outputQualityReview(any(LessonContent.class))
    ).thenReturn(rejectedReview, rejectedReview);

    assertThrows(LessonValidationException.class, () ->
      lessonGenerationService.generateAndSaveLesson(lessonCode)
    );

    verify(aiContentValidation, times(2)).outputQualityReview(
      any(LessonContent.class)
    );
    Mockito.verify(lessonRepository, Mockito.never()).save(any(Lesson.class));

    verify(lessonContentGenerator).generateExplanation(lessonConcept, issues);
    verify(lessonContentGenerator).generateExamples(lessonConcept, issues);
    verify(lessonContentGenerator).generateCommonMistakes(
      lessonConcept,
      issues
    );
    verify(lessonContentGenerator).generateExercises(lessonConcept, issues);
  }

  private void stubGeneratedContent() {
    Mockito.when(
      lessonConceptRepository.findByLessonCode(lessonCode.name())
    ).thenReturn(Optional.of(lessonConcept));
    Mockito.when(
      lessonContentGenerator.generateExplanation(
        Mockito.eq(lessonConcept),
        Mockito.anyList()
      )
    ).thenReturn(
      new LessonExplanation(
        "Explanation",
        "A complete sentence expresses a complete thought."
      )
    );
    Mockito.when(
      lessonContentGenerator.generateExamples(
        Mockito.eq(lessonConcept),
        Mockito.anyList()
      )
    ).thenReturn(
      new LessonExamples(
        "Examples",
        List.of("Birds sing.", "She laughed.", "We went home.")
      )
    );
    Mockito.when(
      lessonContentGenerator.generateCommonMistakes(
        Mockito.eq(lessonConcept),
        Mockito.anyList()
      )
    ).thenReturn(new LessonCommonMistakes("Common mistakes", List.of()));
    Mockito.when(
      lessonContentGenerator.generateExercises(
        Mockito.eq(lessonConcept),
        Mockito.anyList()
      )
    ).thenReturn(List.of());
  }
}
