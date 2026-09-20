package com.grammar.app.grammar_backend.service.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCommonMistakes;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExamples;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExercise;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExplanation;

@Component
public class LessonContentGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LessonContentGenerator.class);
    private final AiResponseOrchestrator orchestrator;

    public LessonContentGenerator(AiResponseOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public LessonExplanation generateExplanation(LessonConcept concept, List<String> previousIssues) {
        String prompt = PromptConstants.USER_CONTENT_SHAPE +
                PromptConstants.USER_RULES +
                """
                Generate a short lesson title and a clear explanation for this concept.
                Return valid JSON only with this schema:
                {
                  "title": "string",
                  "explanation": "string"
                }
                """.formatted(previousIssuesPrompt(previousIssues),
                concept.getName(),
                concept.getCategory(),
                concept.getDifficultyLevel(),
                concept.getGoal());

        LOGGER.info("Generating explanation for concept: {}", concept.getName());
        LOGGER.debug("Prompt for explanation generation: {}", prompt);

        return orchestrator.execute(PromptConstants.SYSTEM_CONTENT_GENERATION,prompt, LessonExplanation.class);
    }

    public LessonExamples generateExamples(LessonConcept concept, List<String> previousIssues) {
        String prompt = PromptConstants.USER_CONTENT_SHAPE + """
                Generate:
                - a short section title
                - exactly 3 clear example sentences that demonstrate this concept
                """ +
                PromptConstants.USER_RULES +
                """
                {
                  "title": "string",
                  "examples": ["string", "string", "string"]
                }
                - The value must be raw JSON
                """.formatted(previousIssuesPrompt(previousIssues),
                concept.getName(),
                concept.getCategory(),
                concept.getDifficultyLevel(),
                concept.getGoal());

        LOGGER.info("Generating examples for concept: {}", concept.getName());
        LOGGER.debug("Prompt for example generation: {}", prompt);

        return orchestrator.execute(PromptConstants.SYSTEM_CONTENT_GENERATION,prompt, LessonExamples.class);
    }

    public LessonCommonMistakes generateCommonMistakes(LessonConcept concept, List<String> previousIssues) {
        String prompt = PromptConstants.USER_CONTENT_SHAPE + """
                Generate:
                - a short section title
                - exactly 3 common mistakes that learners make with this concept, along with explanations and fixes
                - The fix should be a corrected sentence or grammatical rule.
                """ +
                PromptConstants.USER_RULES +
                """
                {
                  "title": "string",
                  "commonMistakes": [
                    {
                      "mistake": "string",
                      "explanation": "string",
                      "fix": "string"
                    },
                    {
                      "mistake": "string",
                      "explanation": "string",
                      "fix": "string"
                    },
                    {
                      "mistake": "string",
                      "explanation": "string",
                      "fix": "string"
                    }
                  ]
                }
                """.formatted(previousIssuesPrompt(previousIssues),
                concept.getName(),
                concept.getCategory(),
                concept.getDifficultyLevel(),
                concept.getGoal());

        LOGGER.info("Generating common mistakes for concept: {}", concept.getName());
        LOGGER.debug("Prompt for common mistakes generation: {}", prompt);

        return orchestrator.execute(PromptConstants.SYSTEM_CONTENT_GENERATION, prompt, LessonCommonMistakes.class);
    }

    public List<LessonExercise> generateExercises(LessonConcept concept, List<String> previousIssues) {
        List<LessonExercise> exercises = new ArrayList<>();
        for (int i = 0; i < concept.getObjectives().size(); i++) {
            String objective = concept.getObjectives().get(i);
            LessonExercise generated = generateExerciseForObjective(concept, objective, previousIssues);
            String id = String.format(
                    "%s_exercise_%03d",
                    concept.getLessonCode(),
                    i + 1);
            exercises.add(generated.withId(id));
        }
        return exercises;
    }

    private String previousIssuesPrompt(List<String> previousIssues) {
        if (previousIssues == null || previousIssues.isEmpty()) {
            return "";
        }

        return """
                      Previous feedback from quality review:
                 %s

                 Fix all of the above issues in this retry.
                 Do not repeat these mistakes.
                """.formatted(
                previousIssues.stream()
                        .map(issue -> "- " + issue)
                        .collect(Collectors.joining("\n")));
    }

    private LessonExercise generateExerciseForObjective(LessonConcept concept, String objective,
            List<String> previousIssues) {
        String prompt = PromptConstants.USER_CONTENT_SHAPE +"""
                Objective to teach: %s

                Choose the single best exercise type for this objective from:
                MULTIPLE_CHOICE, FILL_IN_THE_BLANK, TRUE_FALSE, DRAG_AND_DROP, SENTENCE_ORDERING
                """+
                PromptConstants.USER_EXERCISE_RULES +
                """
                - Return valid JSON only with this schema:
                {
                  "type": "MULTIPLE_CHOICE",
                  "objective": "string",
                  "prompt": "string",
                  "questionText": "string",
                  "options": ["string"],
                  "correctAnswer": "string",
                  "explanation": "string"
                }
                """.formatted(previousIssuesPrompt(previousIssues),
                concept.getName(),
                concept.getCategory(),
                concept.getDifficultyLevel(),
                concept.getGoal(),
                objective);

        LOGGER.info("Generating exercise for concept: {} and objective: {}", concept.getName(), objective);
        LOGGER.debug("Prompt for exercise generation: {}", prompt);

        return orchestrator.execute(PromptConstants.SYSTEM_CONTENT_GENERATION, prompt, LessonExercise.class);
    }

}
