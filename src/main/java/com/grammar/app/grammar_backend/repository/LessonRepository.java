package com.grammar.app.grammar_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grammar.app.grammar_backend.entity.DifficultyLevel;
import com.grammar.app.grammar_backend.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findByTitle(String title);

    Optional<Lesson> findByDifficultyLevel(DifficultyLevel difficultyLevel);

}
