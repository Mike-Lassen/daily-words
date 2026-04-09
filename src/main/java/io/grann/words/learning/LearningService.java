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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningService {
    private final WordRepository wordRepository;
    private final LevelRepository levelRepository;
    private final DeckProgressRepository deckProgressRepository;
    private final ReviewStateRepository reviewStateRepository;

    private final Clock clock;

    public LearningSession startSession(UserSession userSession) {
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        List<Word> words = wordRepository.findUnlockedWordsWithoutReviewState(deckProgress, Pageable.ofSize(5));

        if (words.size() < 1) {
            throw new IllegalStateException("Not enough new words to learn");
        }

        LearningSession session = new LearningSession();
        List<Long> ids = words.stream().map(Word::getId).toList();
        session.setWords(new ArrayList<>(ids));
        return session;
    }

    public void startReview(LearningSession session) {
        session.setPhase(LearningPhase.REVIEW);
        session.setReviewQueue(new ArrayDeque<>(session.getWords()));
        advance(session);
    }

    public void applyRating(LearningSession session, ReviewRating rating) {
        Long wordId = session.getCurrentWord();
        if (rating == ReviewRating.AGAIN) {
            session.getReviewQueue().addLast(wordId);
        }
        session.setShowAnswer(false);
    }

    @Transactional
    public void graduate(UserSession userSession, LearningSession session) {
        Long wordId = session.getCurrentWord();
        Word word = wordRepository.findById(wordId).get();
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextReviewAt = now.plusDays(365);
        ReviewState rs = ReviewState.builder()
                .deckProgress(deckProgress)
                .word(word)
                .level(SrsLevel.LEVEL_6)
                .nextReviewAt(nextReviewAt)
                .lastReviewedAt(now)
                .status(ReviewStateStatus.GRADUATED)
                .build();
        reviewStateRepository.save(rs);
        session.setShowAnswer(false);
    }

    public void advance(LearningSession session) {
        if (!session.getReviewQueue().isEmpty()) {
            session.setCurrentWord(session.getReviewQueue().pollFirst());
        }
    }

    @Transactional
    public void complete(UserSession userSession, LearningSession session) {
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        List<Word> words = session.getWords().stream()
                .map(id -> wordRepository.findById(id).get())
                .toList();
        // 1) Transition learned words to REVIEWING
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime next = now.plusDays(1).toLocalDate().atTime(3,0);

        for (Word word : words) {
            Optional<ReviewState> existingReviewState = reviewStateRepository.findByWordAndDeckProgress(word, deckProgress);
            if (existingReviewState.isEmpty()) {
                ReviewState rs = ReviewState.builder()
                        .deckProgress(deckProgress)
                        .word(word)
                        .level(SrsLevel.LEVEL_1)
                        .nextReviewAt(next)
                        .lastReviewedAt(now)
                        .status(ReviewStateStatus.LEARNING)
                        .build();
                reviewStateRepository.save(rs);
            }
        }
    }
}
