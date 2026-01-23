package io.grann.words.repository;

import io.grann.words.domain.Deck;
import io.grann.words.domain.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findFirstByDeckOrderByOrderIndexAsc(Deck deck);

    Optional<Level> findFirstByDeckAndOrderIndexGreaterThanOrderByOrderIndexAsc(Deck deck, int orderIndex);
}
