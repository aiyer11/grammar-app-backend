package com.grammar.app.grammar_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grammar.app.grammar_backend.entity.DifficultyLevel;
import com.grammar.app.grammar_backend.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findById(Long id);

    Optional<List<Lesson>> findByDifficultyLevel(DifficultyLevel difficultyLevel);

    void deleteById(Long id);
}
