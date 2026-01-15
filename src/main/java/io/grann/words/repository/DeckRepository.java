package io.grann.words.repository;

import io.grann.words.domain.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<Deck, Long> {}
