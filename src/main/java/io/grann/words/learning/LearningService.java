package io.grann.words.learning;

import io.grann.words.domain.*;
import io.grann.words.repository.*;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public LearningSession startSession(UserSession userSession) {
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        List<Word> words = wordRepository.findUnlockedWordsWithoutReviewState(deckProgress, Pageable.ofSize(5));

        if (words.size() < 5) {
            throw new IllegalStateException("Not enough new words to learn");
        }
        for (Word word : words) {
            String kana = word.getKana();
        }

        LearningSession session = new LearningSession();
        session.setDeckProgress(deckProgress);
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
                    .deckProgress(session.getDeckProgress())
                    .word(word)                // owning side (IMPORTANT)
                    .level(SrsLevel.LEVEL_1)   // adapt name to your enum
                    .nextReviewAt(LocalDateTime.now(clock).plusDays(1))
                    .lastReviewedAt(LocalDateTime.now(clock))
                    .build();
            reviewStateRepository.save(rs);
        }



        // 2) If that exhausted the current level, unlock the next level (per deck)
//        Deck deck = session.getWords().getFirst().getLevel().getDeck();
//        DeckProgress progress = deckProgressRepository.findByDeck(deck)
//                .orElseGet(() -> initializeProgress(deck));
//
//        Level currentLevel = levelRepository.findFirstByDeckAndOrderIndexGreaterThanOrderByOrderIndexAsc(
//                        deck,
//                        progress.getCurrentOrderIndex() - 1
//                )
//                .orElse(null);
//
//        // If we can't resolve a current level entity, skip advancing.
//        if (currentLevel == null) {
//            return;
//        }
//
////        long remainingLearningInCurrentLevel = wordRepository.countByLevelAndStatus(currentLevel, WordStatus.LEARNING);
////        if (remainingLearningInCurrentLevel > 0) {
////            return;
////        }
//
//        levelRepository.findFirstByDeckAndOrderIndexGreaterThanOrderByOrderIndexAsc(deck, progress.getCurrentOrderIndex())
//                .ifPresent(nextLevel -> {
//                    progress.setCurrentOrderIndex(nextLevel.getOrderIndex());
//                    deckProgressRepository.save(progress);
//                });
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
