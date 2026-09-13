package com.grammar.app.grammar_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grammar.app.grammar_backend.entity.Lesson;
import com.grammar.app.grammar_backend.exceptions.LessonNotFoundException;
import com.grammar.app.grammar_backend.repository.LessonRepository;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(LessonService.class);

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public Lesson getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with id: " + id));
        LOGGER.info("Retrieved lesson with title: {}", lesson.getTitle());
        return lesson;
    }

    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new LessonNotFoundException("Lesson not found with id: " + id);
        }
        lessonRepository.deleteById(id);
        LOGGER.info("Deleted lesson with id: {}", id);
    }
}
