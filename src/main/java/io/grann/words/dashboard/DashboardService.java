package io.grann.words.dashboard;

import io.grann.words.domain.*;
import io.grann.words.repository.*;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    private final ReviewStateRepository reviewStateRepository;
    private final LevelRepository levelRepository;
    private final UserAccountRepository userAccountRepository;

    public UserSession getUserSession() {
        UserAccount userAccount = userAccountRepository.findByEmail("mike.lassen@gmail.com")
                .orElseThrow(() -> new IllegalStateException("No user exist yet"));
        Deck deck = deckRepository.findByName("Genki")
                .orElseThrow(() -> new IllegalStateException("No decks exist yet"));

        DeckProgress progress = deckProgressRepository.findByDeckAndUserAccount(deck, userAccount)
                .orElseThrow(() -> new IllegalStateException("No deck-progress exist yet"));
        UserSession userSession = new UserSession();
        userSession.setUserAccountId(userAccount.getId());
        userSession.setDeckProgressId(progress.getId());
        return userSession;
    }

    @Transactional
    public DashboardSummary getDashboardSummary() {
        UserAccount userAccount = userAccountRepository.findByEmail("mike.lassen@gmail.com")
                .orElseThrow(() -> new IllegalStateException("No user exist yet"));
        Deck deck = deckRepository.findByName("Genki")
                .orElseThrow(() -> new IllegalStateException("No decks exist yet"));

        DeckProgress progress = deckProgressRepository.findByDeckAndUserAccount(deck, userAccount)
                .orElseThrow(() -> new IllegalStateException("No deck-progress exist yet"));

        Level level = levelRepository.findByDeckAndOrderIndex(deck, progress.getCurrentOrderIndex()).get();

        long total = wordRepository.countByLevel(level);
        List<ReviewState> reviewStates = reviewStateRepository.findByDeckProgressAndWordLevel(progress, level);
        long notInReview = total - reviewStates.size();

        List<SrsLevel> traineeLevels = List.of(SrsLevel.LEVEL_1, SrsLevel.LEVEL_2, SrsLevel.LEVEL_3);
        List<SrsLevel> expertLevels = List.of(SrsLevel.LEVEL_4, SrsLevel.LEVEL_5, SrsLevel.LEVEL_6);

        long trainee = reviewStates.stream()
                .filter(current -> traineeLevels.contains(current.getLevel()))
                .count();
        long expert = reviewStates.stream()
                .filter(current -> expertLevels.contains(current.getLevel()))
                .count();
        LocalDateTime now = LocalDateTime.now(clock);
        long newWordsAvailable = wordRepository.countUnlockedWordsWithoutReviewState(progress);
        long reviewsDue = reviewStateRepository.findDueByDeckProgress(progress, now, Pageable.ofSize(100)).size();
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
