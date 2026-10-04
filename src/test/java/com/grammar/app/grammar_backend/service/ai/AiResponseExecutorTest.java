package com.grammar.app.grammar_backend.service.ai;

import com.grammar.app.grammar_backend.entity.lesson_generation.LessonQualityReview;
import com.grammar.app.grammar_backend.exceptions.AiResponseParsingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiResponseExecutorTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private final AiResponseExecutor aiResponseExecutor = new AiResponseExecutor();

    @BeforeEach
    void setUp() {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(responseSpec);
    }

    @Test
    void testExecute() {
        LessonQualityReview expectedReview = new LessonQualityReview(true, List.of(), List.of());
        when(responseSpec.entity(eq(LessonQualityReview.class), any())).thenReturn(expectedReview);

        LessonQualityReview actualReview = aiResponseExecutor.execute(
                chatClient,
                "system prompt",
                "user prompt",
                LessonQualityReview.class
        );

        assertSame(expectedReview, actualReview);
    }

    @Test
    void testExecuteThrowsException() {
        AiResponseParsingException cause = new AiResponseParsingException("Failed to parse AI response", new RuntimeException("Parsing error"));
        when(responseSpec.entity(eq(LessonQualityReview.class), any())).thenThrow(cause);

        AiResponseParsingException thrown = assertThrows(
                AiResponseParsingException.class,
                () -> aiResponseExecutor.execute(
                        chatClient,
                        "system prompt",
                        "user prompt",
                        LessonQualityReview.class
                )
        );

        assertSame(cause, thrown.getCause());
    }

}
