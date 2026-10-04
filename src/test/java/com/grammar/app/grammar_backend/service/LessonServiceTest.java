package com.grammar.app.grammar_backend.service;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.exceptions.LessonNotFoundException;
import com.grammar.app.grammar_backend.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @InjectMocks
    private LessonService lessonService;

    @Test
   void testGetLessonById() {
       Lesson exepectedLesson = Lesson.builder().id(1L).title("Completed sentences vs fragments").description("Test lesson").build();
       Mockito.when(lessonRepository.findById(1L)).thenReturn(java.util.Optional.of(exepectedLesson));

       Lesson actualLesson = lessonService.getLessonById(1L);
       assertEquals(exepectedLesson.getId(), actualLesson.getId());
       assertEquals(exepectedLesson.getTitle(), actualLesson.getTitle());
       assertEquals(exepectedLesson.getDescription(), actualLesson.getDescription());
    }

    @Test
    void testGetLessonByIdNotFound() {
        Mockito.when(lessonRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        assertThrows(LessonNotFoundException.class, () -> lessonService.getLessonById(1L));
    }

    @Test
    void testDeleteLesson() {
        Mockito.when(lessonRepository.existsById(1L)).thenReturn(true);
        lessonService.deleteLesson(1L);
        Mockito.verify(lessonRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    void testDeleteLessonNotFound() {
        Mockito.when(lessonRepository.existsById(1L)).thenReturn(false);
        assertThrows(LessonNotFoundException.class, () -> lessonService.deleteLesson(1L));
    }

}
