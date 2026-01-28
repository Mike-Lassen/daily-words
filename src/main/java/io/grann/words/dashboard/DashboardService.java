package io.grann.words.dashboard;

import io.grann.words.domain.*;
import io.grann.words.repository.DeckProgressRepository;
import io.grann.words.repository.DeckRepository;
import io.grann.words.repository.LevelRepository;
import io.grann.words.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DashboardService {

    private final DeckRepository deckRepository;
    private final WordRepository wordRepository;
    private final Clock clock;
    private final DeckProgressRepository deckProgressRepository;
    private final LevelRepository levelRepository;

    @Transactional
    public DashboardSummary getDashboardSummary() {
        Optional<Deck> genki = deckRepository.findByName("Genki");
        Deck deck = genki.orElse( deckRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No decks exist yet")));

        DeckProgress progress = deckProgressRepository.findByDeck(deck)
                .orElseGet(() -> initializeProgress(deck));

        Level level = levelRepository.findByDeckAndOrderIndex(deck, progress.getCurrentOrderIndex()).get();
        long total = wordRepository.countByLevel(level);
        long notInReview = wordRepository.countByLevelAndStatus(level, WordStatus.LEARNING);

        List<SrsLevel> traineeLevels = List.of(SrsLevel.LEVEL_1, SrsLevel.LEVEL_2, SrsLevel.LEVEL_3);
        List<SrsLevel> expertLevels = List.of(SrsLevel.LEVEL_4, SrsLevel.LEVEL_5, SrsLevel.LEVEL_6);

        long trainee = wordRepository.countByLevelAndReviewStateLevelIn(level, traineeLevels);
        long expert = wordRepository.countByLevelAndReviewStateLevelIn(level, expertLevels);


        LocalDateTime now = LocalDateTime.now(clock);

        long newWordsAvailable = wordRepository.countByLevelDeckAndStatus(deck, WordStatus.LEARNING);
        long reviewsDue = wordRepository.countWordsDueForReview(now);

        return new DashboardSummary(level.getName(), newWordsAvailable, reviewsDue, total, notInReview, trainee, expert);
    }

    protected DeckProgress initializeProgress(Deck deck) {
        Level firstLevel = levelRepository.findFirstByDeckOrderByOrderIndexAsc(deck)
                .orElseThrow(() -> new IllegalStateException("Deck has no levels: " + deck.getName()));

        DeckProgress progress = DeckProgress.builder()
                .deck(deck)
                .currentOrderIndex(firstLevel.getOrderIndex())
                .build();

        return deckProgressRepository.save(progress);
    }

    public record DashboardSummary(String levelName,
                                   long newWordsAvailable,
                                   long reviewsDue,
                                   long total,
                                   long notInReview,
                                   long trainee,
                                   long expert) {}
}
