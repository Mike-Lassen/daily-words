package io.grann.words.repository;

import io.grann.words.domain.ReviewState;
import io.grann.words.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewState, Long> {
    Optional<ReviewState> findByWord(Word word);
}
