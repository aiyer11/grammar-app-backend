package com.grammar.app.grammar_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCommonMistakes;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExamples;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExercise;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonExplanation;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonSection;
import com.grammar.app.grammar_backend.service.ai.AiResponseOrchestrator;

@Service
public class LessonGenerationService {

  private final Logger LOGGER = LoggerFactory.getLogger(LessonGenerationService.class);
  private final AiResponseOrchestrator orchestrator;

  public LessonGenerationService(AiResponseOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  private LessonExplanation generateExplaination(LessonConcept concept) {
    String prompt = """
        You are an expert English grammar teacher.

        Concept:
        - Name: %s
        - Category: %s
        - Difficulty: %s
        - Goal: %s

        Rules:
        - Keep them appropriate for the difficulty level
        - Make them realistic and grammar-correct
        - The examples should directly show the concept in use
        - Return valid JSON only with this schema:
        - No markdown fences.
        - No commentary.
        - No text before or after the JSON.
        - No code blocks.
        - No trailing commas.
        - The output must be a single JSON object.

        Generate a short lesson title and a clear explanation for this concept.
        Return valid JSON only with this schema:
        {
          "title": "string",
          "explanation": "string"
        }
        """.formatted(
        concept.getName(),
        concept.getCategory(),
        concept.getDifficultyLevel(),
        concept.getGoal());

    LOGGER.info("Generating explanation for concept: {}", concept.getName());
    LOGGER.debug("Prompt for explanation generation: {}", prompt);

    return orchestrator.execute(prompt, LessonExplanation.class);
  }

  private LessonExamples generateExamples(LessonConcept concept) {
    String prompt = """
        You are an expert English grammar teacher.

        Concept:
        - Name: %s
        - Category: %s
        - Difficulty: %s
        - Goal: %s

        Generate:
        - a short section title
        - exactly 3 clear example sentences that demonstrate this concept

        Rules:
        - Keep them appropriate for the difficulty level
        - Make them realistic and grammar-correct
        - The examples should directly show the concept in use
        - Return valid JSON only with this schema:
        - No markdown fences.
        - No commentary.
        - No text before or after the JSON.
        - No code blocks.
        - No trailing commas.
        - The output must be a single JSON object.
        {
          "title": "string",
          "examples": ["string", "string", "string"]
        }
        - The value must be raw JSON
        """.formatted(
        concept.getName(),
        concept.getCategory(),
        concept.getDifficultyLevel(),
        concept.getGoal());

    LOGGER.info("Generating examples for concept: {}", concept.getName());
    LOGGER.debug("Prompt for example generation: {}", prompt);

    return orchestrator.execute(prompt, LessonExamples.class);
  }

  private LessonCommonMistakes generateCommonMistakes(LessonConcept concept) {
    String prompt = """
        You are an expert English grammar teacher.

        Concept:
        - Name: %s
        - Category: %s
        - Difficulty: %s
        - Goal: %s

        Generate:
        - a short section title
        - exactly 3 common mistakes that learners make with this concept, along with explanations and fixes
        - The fix should be a corrected sentence or grammatical rule.

        Rules:
        - Keep them appropriate for the difficulty level
        - Make them realistic and relevant to the concept
        - Return valid JSON only with this schema:
        - No markdown fences.
        - No commentary.
        - No text before or after the JSON.
        - No code blocks.
        - No trailing commas.
        - The output must be a single JSON object.
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
        """.formatted(
        concept.getName(),
        concept.getCategory(),
        concept.getDifficultyLevel(),
        concept.getGoal());

    LOGGER.info("Generating common mistakes for concept: {}", concept.getName());
    LOGGER.debug("Prompt for common mistakes generation: {}", prompt);

    return orchestrator.execute(prompt, LessonCommonMistakes.class);
  }

  private LessonExercise generateExerciseForObjective(LessonConcept concept, String objective) {
    String prompt = """
        You are an expert English grammar teacher.

        Concept:
        - Name: %s
        - Category: %s
        - Difficulty: %s
        - Goal: %s

        Objective to teach:
        %s

        Choose the single best exercise type for this objective from:
        MULTIPLE_CHOICE, FILL_IN_THE_BLANK, TRUE_FALSE, DRAG_AND_DROP, SENTENCE_ORDERING

        Rules:
        - Generate exactly one exercise.
        - The exercise must directly test this objective.
        - Pick the best format for the objective.
        - Keep it appropriate for the difficulty level.
        - Return valid JSON only with this schema:

        {
          "id": "string",
          "type": "MULTIPLE_CHOICE",
          "objective": "string",
          "prompt": "string",
          "questionText": "string",
          "options": ["string"],
          "correctAnswer": "string",
          "explanation": "string"
        }
        """.formatted(
        concept.getName(),
        concept.getCategory(),
        concept.getDifficultyLevel(),
        concept.getGoal(),
        objective);

    LOGGER.info("Generating exercise for concept: {} and objective: {}", concept.getName(), objective);
    LOGGER.debug("Prompt for exercise generation: {}", prompt);

    return orchestrator.execute(prompt, LessonExercise.class);
  }

  private List<LessonExercise> generateExercises(LessonConcept concept) {
    return concept.getObjectives().stream()
        .map(objective -> generateExerciseForObjective(concept, objective))
        .toList();
  }

  private LessonSection createLessonSection(String type, String title, String content) {
    return new LessonSection(type, title, content);
  }

  private List<LessonSection> createLessonSections(LessonExplanation explanation, LessonExamples examples,
      LessonCommonMistakes commonMistakes) {

    String commonMistakesText = commonMistakes.commonMistakes().stream()
        .map(item -> item.mistake() + " — " + item.explanation() + " Fix: " + item.fix())
        .collect(Collectors.joining("\n"));

    return List.of(
        createLessonSection("EXPLANATION", explanation.title(), explanation.explanation()),
        createLessonSection("EXAMPLES", examples.title(), String.join("\n", examples.examples())),
        createLessonSection("COMMON_MISTAKES", commonMistakes.title(), commonMistakesText));
  }

  public LessonContent buildLessonContent(LessonConcept concept) {
    LessonExplanation explanation = generateExplaination(concept);
    LessonExamples examples = generateExamples(concept);
    LessonCommonMistakes commonMistakes = generateCommonMistakes(concept);
    List<LessonExercise> exercises = generateExercises(concept);

    List<LessonSection> sections = createLessonSections(explanation, examples, commonMistakes);

    LessonContent lessonContent = new LessonContent(
        concept.getName(),
        concept.getGoal(),
        explanation.explanation(),
        sections,
        exercises);

    LOGGER.info("Generated lesson content for concept: {}", concept.getName());
    LOGGER.debug("Lesson content: {}", lessonContent);

    return lessonContent;

  }
}
