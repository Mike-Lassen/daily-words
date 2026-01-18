package io.grann.words.repository;

import io.grann.words.domain.WordAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordAnnotationRepository extends JpaRepository<WordAnnotation, Long> {
}
