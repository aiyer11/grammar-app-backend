package com.grammar.app.grammar_backend.service;

import com.grammar.app.grammar_backend.entity.lesson_generation.LessonCode;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonQualityReview;
import com.grammar.app.grammar_backend.repository.LessonConceptRepository;
import com.grammar.app.grammar_backend.repository.LessonRepository;
import com.grammar.app.grammar_backend.service.ai.AiContentValidation;
import com.grammar.app.grammar_backend.service.ai.LessonContentGenerator;
import com.grammar.app.grammar_backend.util.ObjectMapperUtility;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class LessonGenerationServiceTest {

    @Mock private LessonContentGenerator lessonContentGenerator;
    @Mock private LessonConceptRepository lessonConceptRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private AiContentValidation aiContentValidation;
    @InjectMocks private LessonGenerationService lessonGenerationService;


    @Test
    void testGenerateAndSaveLesson(){
        LessonCode lessonCode = LessonCode.COMPLETE_SENTENCES_VS_FRAGMENTS;
        LessonConcept lessonConcept = ObjectMapperUtility.convertFromJson("src/test/resources/mockJson/LessonConcept.json", LessonConcept.class);
        LessonContent lessonContent = ObjectMapperUtility.convertFromJson("src/test/resources/mockJson/LessonContent.json", LessonContent.class);
        LessonQualityReview approvedReview =
                new LessonQualityReview(true, List.of(), List.of());
        Mockito.when(lessonConceptRepository.findByLessonCode(lessonCode.name())).thenReturn(Optional.of(lessonConcept));
        Mockito.when(lessonContentGenerator.generateExplanation(Mockito.any(), Mockito.any())).thenReturn(lessonContent.sections().get(0));
    }



}
