package io.grann.words.repository;

import io.grann.words.domain.Deck;
import io.grann.words.domain.DeckProgress;
import io.grann.words.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckProgressRepository extends JpaRepository<DeckProgress, Long> {
    Optional<DeckProgress> findByDeck(Deck deck);
    Optional<DeckProgress> findByUserAccountAndDeck(UserAccount userAccount, Deck deck);
    List<DeckProgress> findByUserAccount(UserAccount userAccount);

}
