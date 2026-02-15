package io.grann.words.review;

import io.grann.words.domain.*;
import io.grann.words.repository.DeckProgressRepository;
import io.grann.words.repository.LevelRepository;
import io.grann.words.repository.ReviewStateRepository;
import io.grann.words.repository.WordRepository;
import io.grann.words.session.UserSession;
import io.grann.words.srs.Srs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final WordRepository wordRepository;
    private final ReviewStateRepository reviewStateRepository;
    private final DeckProgressRepository deckProgressRepository;
    private final LevelRepository levelRepository;
    private final Clock clock;

    public ReviewSession startSession(UserSession userSession) {
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        LocalDateTime now = LocalDateTime.now(clock);
        List<ReviewState> reviewStates = reviewStateRepository.findDueByDeckProgress(deckProgress, now, Pageable.ofSize(15));

        ReviewSession session = new ReviewSession();
        session.setTotalCount(reviewStates.size());

        List<Long> ids = reviewStates.stream().map(ReviewState::getId).toList();
        session.setReviewQueue(new ArrayDeque<>(ids));

        return session;
    }

    @Transactional
    public void applyRating(ReviewSession reviewSession, ReviewRating rating) {
        Long reviewStateId = reviewSession.getReviewQueue().pollFirst();
        if (reviewStateId == null) {
            return;
        }

        ReviewState reviewState = reviewStateRepository.findById(reviewStateId).get();

        SrsLevel currentLevel = reviewState.getLevel();
        SrsLevel nextLevel =
                (rating == ReviewRating.GOOD)
                        ? Srs.onGood(currentLevel)
                        : Srs.onAgain(currentLevel);
        LocalDateTime now = LocalDateTime.now(clock);
        reviewState.setLastReviewedAt(now);

        if (rating == ReviewRating.GOOD && nextLevel.isLastLevel()) {
            reviewState.setStatus(ReviewStateStatus.GRADUATED);
        } else {
            reviewState.setLevel(nextLevel);
            LocalDateTime nextReviewAt = now.plus(nextLevel.getInterval());
            reviewState.setNextReviewAt(nextReviewAt);
        }

        if (rating == ReviewRating.AGAIN) {
            reviewSession.getReviewQueue().addLast(reviewStateId);
        }

        reviewSession.setShowAnswer(false);
    }

    @Transactional
    public void complete(UserSession userSession, ReviewSession reviewSession) {
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        Level currentLevel = levelRepository.findByDeckAndOrderIndex(
                        deckProgress.getDeck(),
                        deckProgress.getCurrentOrderIndex()
                )
                .orElse(null);

        if (currentLevel == null) {
            return;
        }

        long total = wordRepository.countByLevel(currentLevel);
        List<ReviewState> reviewStates = reviewStateRepository.findByDeckProgressAndWordLevel(deckProgress, currentLevel);
        long notInReview = total - reviewStates.size();

        if (notInReview > 0) {
            return;
        }

        List<SrsLevel> traineeLevels = SrsLevel.getTraineeLevels();
        List<SrsLevel> expertLevels = SrsLevel.getExpertLevels();

        long trainee = reviewStates.stream().filter(rs -> traineeLevels.contains(rs.getLevel())).count();
        long expert = reviewStates.stream().filter(rs -> expertLevels.contains(rs.getLevel())).count();
        double percentage = (expert * 100) / total;
        if (percentage < 80) {
            return;
        }

        levelRepository.findFirstByDeckAndOrderIndexGreaterThanOrderByOrderIndexAsc(
                        deckProgress.getDeck(),
                        deckProgress.getCurrentOrderIndex()
                )
                .ifPresent(nextLevel -> {
                    deckProgress.setCurrentOrderIndex(nextLevel.getOrderIndex());
                });
    }

}
