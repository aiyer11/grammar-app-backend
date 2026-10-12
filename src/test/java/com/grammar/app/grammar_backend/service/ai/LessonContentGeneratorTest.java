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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void testGenerateExplanation() {
        LessonExplanation expectedExplanation = new LessonExplanation("Sample Title", "Sample Explanation");
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExplanation.class))).thenReturn(expectedExplanation);
        LessonExplanation actualExplanation = lessonContentGenerator.generateExplanation(lessonConcept, null);

        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExplanation.class)
        );
        String prompt = promptCaptor.getValue();
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_RULES));
        assertSame(expectedExplanation, actualExplanation);
    }

    @Test
    void testGenerateExplanationPreviousIssues() {
        List<String> previousIssues = List.of("Issue 1", "Issue 2");
        LessonExplanation expectedExplanation = new LessonExplanation("Sample Title", "Sample Explanation");
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExplanation.class))).thenReturn(expectedExplanation);
        LessonExplanation actualExplanation = lessonContentGenerator.generateExplanation(lessonConcept, previousIssues);

        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExplanation.class)
        );
        String prompt = promptCaptor.getValue();
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_RULES));
        for (String issue : previousIssues) {
            assertTrue(prompt.contains(issue));
        }
        assertSame(expectedExplanation, actualExplanation);
    }

    @Test
    void testGenerateExamples() {
        LessonExamples expectedExamples = new LessonExamples("Sample Title", List.of("Example 1", "Example 2", "Example 3"));
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExamples.class))).thenReturn(expectedExamples);
        LessonExamples actualExamples = lessonContentGenerator.generateExamples(lessonConcept, null);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExamples.class)
        );

        String prompt = promptCaptor.getValue();

        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_RULES));
        assertSame(expectedExamples, actualExamples);
    }

    @Test
    void testGenerateExamplesPreviousIssues() {
        List<String> previousIssues = List.of("Issue 1", "Issue 2");
        LessonExamples expectedExamples = new LessonExamples("Sample Title", List.of("Example 1", "Example 2", "Example 3"));
        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonExamples.class))).thenReturn(expectedExamples);
        LessonExamples actualExamples = lessonContentGenerator.generateExamples(lessonConcept, previousIssues);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExamples.class)
        );

        String prompt = promptCaptor.getValue();
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_RULES));
        for (String issue : previousIssues) {
            assertTrue(prompt.contains(issue));
        }
        assertSame(expectedExamples, actualExamples);
    }

    @Test
    void testGenerateExerciseForObjective() {
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
        LessonExercise actualExercise = lessonContentGenerator.generateExerciseForObjective(lessonConcept, objective, null);

        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExercise.class)
        );
        String prompt = promptCaptor.getValue();
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_EXERCISE_RULES));
        assertTrue(prompt.contains(objective));
        assertSame(expectedExercise, actualExercise);
    }

    @Test
    void testGenerateExerciseForObjectivePreviousIssues() {
        String objective = "Identify the correct use of past tense in a sentence.";
        List<String> previousIssues = List.of("Issue 1", "Issue 2");
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

        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonExercise.class)
        );
        String prompt = promptCaptor.getValue();
        lessonConceptAssertions(prompt, lessonConcept);
        assertTrue(prompt.contains(PromptConstants.USER_EXERCISE_RULES));
        assertTrue(prompt.contains(objective));
        for (String issue : previousIssues) {
            assertTrue(prompt.contains(issue));
        }
        assertSame(expectedExercise, actualExercise);
    }

    @Test
    void testGenerateExercises() {
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

        List<LessonExercise> actualExercises = lessonContentGenerator.generateExercises(lessonConcept, null);
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
            assertEquals(expectedExercises.get(i), actualExercises.get(i));
        }
    }

    @Test
    void testGenerateExercisesPreviousIssues() {
        List<String> previousIssues = List.of("Issue 1", "Issue 2");
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
            for (String issue : previousIssues) {
                assertTrue(prompts.get(i).contains(issue));
            }
            assertEquals(expectedExercises.get(i), actualExercises.get(i));
        }
    }

    @Test
    void testGenerateCommonMistakes() {
        List<CommonMistake> expectedCommonMistakes = List.of(
            new CommonMistake("Mistake 1", "Explanation 1", "Fix 1"),
            new CommonMistake("Mistake 2", "Explanation 2", "Fix 2"),
            new CommonMistake("Mistake 3", "Explanation 3", "Fix 3")
        );

        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonCommonMistakes.class))).thenReturn(
            new LessonCommonMistakes("Common Mistakes", expectedCommonMistakes)
        );

        LessonCommonMistakes actualCommonMistakes = lessonContentGenerator.generateCommonMistakes(lessonConcept, null);
        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonCommonMistakes.class)
        );
        lessonConceptAssertions(promptCaptor.getValue(), lessonConcept);
        assertEquals(expectedCommonMistakes, actualCommonMistakes.commonMistakes());
    }

    @Test
    void testGenerateCommonMistakesPreviousIssues() {
        List<String> previousIssues = List.of("Issue 1", "Issue 2");
        List<CommonMistake> expectedCommonMistakes = List.of(
            new CommonMistake("Mistake 1", "Explanation 1", "Fix 1"),
            new CommonMistake("Mistake 2", "Explanation 2", "Fix 2"),
            new CommonMistake("Mistake 3", "Explanation 3", "Fix 3")
        );

        when(aiResponseOrchestrator.execute(anyString(), anyString(), eq(LessonCommonMistakes.class))).thenReturn(
            new LessonCommonMistakes("Common Mistakes", expectedCommonMistakes)
        );

        LessonCommonMistakes actualCommonMistakes = lessonContentGenerator.generateCommonMistakes(lessonConcept, previousIssues);
        verify(aiResponseOrchestrator).execute(
            eq(PromptConstants.SYSTEM_CONTENT_GENERATION),
            promptCaptor.capture(),
            eq(LessonCommonMistakes.class)
        );
        String prompt = promptCaptor.getValue();
        lessonConceptAssertions(prompt, lessonConcept);
        for (String issue : previousIssues) {
            assertTrue(prompt.contains(issue));
        }
        assertEquals(expectedCommonMistakes, actualCommonMistakes.commonMistakes());
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
