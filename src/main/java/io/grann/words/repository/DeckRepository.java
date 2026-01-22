package io.grann.words.repository;

import io.grann.words.domain.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional<Deck> findByName(String name);
}
