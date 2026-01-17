package io.grann.words.repository;

import io.grann.words.domain.Word;
import io.grann.words.domain.WordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findTop5ByStatusOrderByIdAsc(WordStatus status);
}
