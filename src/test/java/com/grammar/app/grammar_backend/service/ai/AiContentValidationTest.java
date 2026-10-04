package com.grammar.app.grammar_backend.service.ai;

import com.grammar.app.grammar_backend.entity.lesson_generation.LessonContent;
import com.grammar.app.grammar_backend.entity.lesson_generation.LessonQualityReview;
import com.grammar.app.grammar_backend.util.ObjectMapperUtility;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiContentValidationTest {

    @Mock
    private AiResponseExecutor executor;
    @Mock
    private ChatClient chatClient;
    @InjectMocks
    private AiContentValidation aiContentValidation;

    @Test
    void testValidateContent() {
        LessonContent lessonContent = ObjectMapperUtility.convertFromJson(
                "mockJson/LessonContent.json",
                LessonContent.class
        );

        LessonQualityReview expectedReview = new LessonQualityReview(true, List.of(), List.of());

        when(executor.execute(eq(chatClient),eq(PromptConstants.SYSTEM_CONTENT_VALIDATION), anyString(), eq(LessonQualityReview.class)))
                .thenReturn(expectedReview);

        LessonQualityReview actualReview = aiContentValidation.outputQualityReview(lessonContent);
        assertSame(expectedReview, actualReview);
    }

}
