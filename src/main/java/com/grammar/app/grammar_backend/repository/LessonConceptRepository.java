package com.grammar.app.grammar_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grammar.app.grammar_backend.entity.lesson_generation.LessonConcept;

public interface LessonConceptRepository extends JpaRepository<LessonConcept, Long> {

    Optional<LessonConcept> findByLessonCode(String lessonCode);

}
