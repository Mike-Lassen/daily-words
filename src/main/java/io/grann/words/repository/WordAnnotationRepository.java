package io.grann.words.repository;

import io.grann.words.domain.Word;
import io.grann.words.domain.WordAnnotation;
import io.grann.words.domain.WordAnnotationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordAnnotationRepository extends JpaRepository<WordAnnotation, Long> {
    boolean existsByWordAndType(Word word, WordAnnotationType type);
}
