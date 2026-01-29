package io.grann.words.learning;

import io.grann.words.domain.*;
import io.grann.words.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningService {

    private final WordRepository wordRepository;
    private final DeckRepository deckRepository;
    private final LevelRepository levelRepository;
    private final DeckProgressRepository deckProgressRepository;
    private final ReviewStateRepository reviewStateRepository;

    private final Clock clock;

    public LearningSession startSession() {
        // MVP: pick the first deck (single-user, single-deck UX for now)
        Deck deck = deckRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No decks exist yet"));

        DeckProgress progress = deckProgressRepository.findByDeck(deck)
                .orElseGet(() -> initializeProgress(deck));

        List<Long> ids = wordRepository.findLearningIdsAvailableInDeckUpToOrderIndex(
                deck.getId(),
                progress.getCurrentOrderIndex(),
                PageRequest.of(0, 5)
        );

        if (ids.size() < 5) {
            throw new IllegalStateException("Not enough new words to learn");
        }

        List<Word> words = wordRepository.findByIdInWithAnnotations(ids);

        // Preserve the deterministic ordering from the ID query (without O(n^2) indexOf)
        var positionById = new HashMap<Long, Integer>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            positionById.put(ids.get(i), i);
        }
        words.sort(java.util.Comparator.comparingInt(w -> positionById.getOrDefault(w.getId(), Integer.MAX_VALUE)));

        LearningSession session = new LearningSession();
        session.setWords(words);
        return session;
    }

    public void startReview(LearningSession session) {
        session.setPhase(LearningPhase.REVIEW);
        session.setReviewQueue(new ArrayDeque<>(session.getWords()));
        advance(session);
    }

    public void applyRating(LearningSession session, ReviewRating rating) {
        Word word = session.getCurrentWord();
        if (rating == ReviewRating.AGAIN) {
            session.getReviewQueue().addLast(word);
        }
        // GOOD → word is simply discarded

        session.setShowAnswer(false);
    }

    public void advance(LearningSession session) {
        if (!session.getReviewQueue().isEmpty()) {
            session.setCurrentWord(session.getReviewQueue().pollFirst());
        }
    }

    @Transactional
    public void complete(LearningSession session) {
        // 1) Transition learned words to REVIEWING
        for (Word word : session.getWords()) {
            ReviewState rs = ReviewState.builder()
                    .word(word)                // owning side (IMPORTANT)
                    .level(SrsLevel.LEVEL_1)   // adapt name to your enum
                    .nextReviewAt(LocalDateTime.now(clock).plusDays(1))
                    .lastReviewedAt(null)
                    .build();
            reviewStateRepository.save(rs);
        }
        wordRepository.saveAll(session.getWords()); // merge + cascade

        // 2) If that exhausted the current level, unlock the next level (per deck)
        Deck deck = session.getWords().getFirst().getLevel().getDeck();
        DeckProgress progress = deckProgressRepository.findByDeck(deck)
                .orElseGet(() -> initializeProgress(deck));

        Level currentLevel = levelRepository.findFirstByDeckAndOrderIndexGreaterThanOrderByOrderIndexAsc(
                        deck,
                        progress.getCurrentOrderIndex() - 1
                )
                .orElse(null);

        // If we can't resolve a current level entity, skip advancing.
        if (currentLevel == null) {
            return;
        }

        long remainingLearningInCurrentLevel = wordRepository.countByLevelAndStatus(currentLevel, WordStatus.LEARNING);
        if (remainingLearningInCurrentLevel > 0) {
            return;
        }

        levelRepository.findFirstByDeckAndOrderIndexGreaterThanOrderByOrderIndexAsc(deck, progress.getCurrentOrderIndex())
                .ifPresent(nextLevel -> {
                    progress.setCurrentOrderIndex(nextLevel.getOrderIndex());
                    deckProgressRepository.save(progress);
                });
    }

    @Transactional
    protected DeckProgress initializeProgress(Deck deck) {
        Level firstLevel = levelRepository.findFirstByDeckOrderByOrderIndexAsc(deck)
                .orElseThrow(() -> new IllegalStateException("Deck has no levels: " + deck.getName()));

        DeckProgress progress = DeckProgress.builder()
                .deck(deck)
                .currentOrderIndex(firstLevel.getOrderIndex())
                .build();

        return deckProgressRepository.save(progress);
    }
}
