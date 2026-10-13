package com.grammar.app.grammar_backend.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grammar.app.grammar_backend.entity.lesson_generation.ExerciseType;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExercise;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.CommonMistake;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonCommonMistakes;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonExamples;
import com.grammar.app.grammar_backend.entity.lesson_generation.lesson_section.LessonExplanation;
import com.grammar.app.grammar_backend.util.ObjectMapperUtility;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonContentGeneratorTest {

    @Mock
    private AiResponseOrchestrator aiResponseOrchestrator;

    @InjectMocks
    private LessonContentGenerator lessonContentGenerator;

    @Captor
    private ArgumentCaptor<String> promptCaptor;

    private LessonConcept lessonConcept = ObjectMapperUtility.convertFromJson("mockJson/LessonConcept.json", LessonConcept.class);

    @ParameterizedTest
    @MethodSource("previousIssueCases")
    void testGenerateExplanation(List<String> previousIssues) {
        LessonExplanation expectedExplanation = new LessonExplanation("Sample Title", "Sample Explanation");
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExplanation.class))).thenReturn(expectedExplanation);
        LessonExplanation actualExplanation = lessonContentGenerator.generateExplanation(lessonConcept, previousIssues);

        String prompt = capturePrompt(LessonExplanation.class);
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_RULES));
        assertPreviousIssuesInPrompt(prompt, previousIssues);
        assertSame(expectedExplanation, actualExplanation);
    }

    @ParameterizedTest
    @MethodSource("previousIssueCases")
    void testGenerateExamples(List<String> previousIssues) {
        LessonExamples expectedExamples = new LessonExamples("Sample Title", List.of("Example 1", "Example 2", "Example 3"));
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExamples.class))).thenReturn(expectedExamples);
        LessonExamples actualExamples = lessonContentGenerator.generateExamples(lessonConcept, previousIssues);

        String prompt = capturePrompt(LessonExamples.class);
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_RULES));
        assertPreviousIssuesInPrompt(prompt, previousIssues);
        assertSame(expectedExamples, actualExamples);
    }

    @ParameterizedTest
    @MethodSource("previousIssueCases")
    void testGenerateExerciseForObjective(List<String> previousIssues) {
        String objective = "Identify the correct use of past tense in a sentence.";
        LessonExercise expectedExercise = new LessonExercise(
            "exercise-1",
            ExerciseType.MULTIPLE_CHOICE,
            objective,
            "Choose the correct past tense form of the verb in the following sentence.",
            "She (go) to the store yesterday.",
            List.of("goes", "went", "gone", "going"),
            "went",
            "The correct past tense form of 'go' is 'went'."
        );
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExercise.class))).thenReturn(expectedExercise);
        LessonExercise actualExercise = lessonContentGenerator.generateExerciseForObjective(lessonConcept, objective, previousIssues);

        String prompt = capturePrompt(LessonExercise.class);
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_EXERCISE_RULES));
        assertTrue(prompt.contains(objective));
        assertPreviousIssuesInPrompt(prompt, previousIssues);
        assertSame(expectedExercise, actualExercise);
    }

    @ParameterizedTest
    @MethodSource("previousIssueCases")
    void testGenerateExercises(List<String> previousIssues) {
        List<LessonExercise> expectedExercises = List.of(
            new LessonExercise(
                "COMPLETE_SENTENCES_VS_FRAGMENTS_exercise_001",
                ExerciseType.MULTIPLE_CHOICE,
                "Identify the correct use of past tense in a sentence.",
                "Choose the correct past tense form of the verb in the following sentence.",
                "She (go) to the store yesterday.",
                List.of("goes", "went", "gone", "going"),
                "went",
                "The correct past tense form of 'go' is 'went'."
            ),
            new LessonExercise(
                "COMPLETE_SENTENCES_VS_FRAGMENTS_exercise_002",
                ExerciseType.FILL_IN_THE_BLANK,
                "Identify the correct use of past tense in a sentence.",
                "Fill in the blank with the correct past tense form of the verb.",
                "He (eat) dinner at 7 PM last night.",
                List.of("eats", "ate", "eaten", "eating"),
                "ate",
                "The correct past tense form of 'eat' is 'ate'."
            ),
            new LessonExercise(
                "COMPLETE_SENTENCES_VS_FRAGMENTS_exercise_003",
                ExerciseType.TRUE_FALSE,
                "Identify the correct use of past tense in a sentence.",
                "Determine if the following statement is true or false.",
                "The past tense of 'run' is 'ran'.",
                List.of("TRUE", "FALSE"),
                "TRUE",
                "The past tense of 'run' is indeed 'ran'."
            )
        );

        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExercise.class))).thenReturn(
            expectedExercises.get(0),
            expectedExercises.get(1),
            expectedExercises.get(2)
        );

        List<LessonExercise> actualExercises = lessonContentGenerator.generateExercises(lessonConcept, previousIssues);
        int objectiveCount = lessonConcept.getObjectives().size();

        verify(aiResponseOrchestrator, times(objectiveCount)).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExercise.class)
        );

        List<String> prompts = promptCaptor.getAllValues();
        assertEquals(objectiveCount, prompts.size());

        for (int i = 0; i < expectedExercises.size(); i++) {
            assertTrue(prompts.get(i).contains(lessonConcept.getObjectives().get(i)));
            assertPreviousIssuesInPrompt(prompts.get(i), previousIssues);
            assertEquals(expectedExercises.get(i), actualExercises.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("previousIssueCases")
    void testGenerateCommonMistakes(List<String> previousIssues) {
        List<CommonMistake> expectedCommonMistakes = List.of(
            new CommonMistake("Mistake 1", "Explanation 1", "Fix 1"),
            new CommonMistake("Mistake 2", "Explanation 2", "Fix 2"),
            new CommonMistake("Mistake 3", "Explanation 3", "Fix 3")
        );

        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonCommonMistakes.class))).thenReturn(
            new LessonCommonMistakes("Common Mistakes", expectedCommonMistakes)
        );

        LessonCommonMistakes actualCommonMistakes = lessonContentGenerator.generateCommonMistakes(lessonConcept, previousIssues);
        String prompt = capturePrompt(LessonCommonMistakes.class);
        lessonConceptAssertions(prompt, lessonConcept);
        assertPreviousIssuesInPrompt(prompt, previousIssues);
        assertEquals(expectedCommonMistakes, actualCommonMistakes.commonMistakes());
    }

    private static Stream<Arguments> previousIssueCases() {
        return Stream.of(Arguments.of((Object) null), Arguments.of(List.of("Issue 1", "Issue 2")));
    }

    private void assertPreviousIssuesInPrompt(String prompt, List<String> previousIssues) {
        if (previousIssues != null) {
            for (String issue : previousIssues) {
                assertTrue(prompt.contains(issue));
            }
        }
    }

    private <T> String capturePrompt(Class<T> responseType) {
        verify(aiResponseOrchestrator).execute(eq(PromptConstants.SYSTEM_CONTENT_GENERATION), promptCaptor.capture(), eq(responseType));
        return promptCaptor.getValue();
    }

    private void lessonConceptAssertions(String prompt, LessonConcept lessonConcept) {
        assertTrue(
            prompt.contains(
                PromptConstants.USER_CONTENT_SHAPE.formatted(
                    "",
                    lessonConcept.getName(),
                    lessonConcept.getCategory(),
                    lessonConcept.getDifficultyLevel(),
                    lessonConcept.getGoal()
                )
            )
        );
        assertTrue(prompt.contains("Name: " + lessonConcept.getName()));
        assertTrue(prompt.contains("Category: " + lessonConcept.getCategory()));
        assertTrue(prompt.contains("Difficulty: " + lessonConcept.getDifficultyLevel()));
        assertTrue(prompt.contains(lessonConcept.getGoal()));
    }
}
