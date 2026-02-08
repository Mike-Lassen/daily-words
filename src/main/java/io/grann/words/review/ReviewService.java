package io.grann.words.review;

import io.grann.words.domain.*;
import io.grann.words.repository.DeckProgressRepository;
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
    private final Clock clock;

    public ReviewSession startSession(UserSession userSession) {
        DeckProgress deckProgress = deckProgressRepository.findById(userSession.getDeckProgressId()).get();
        LocalDateTime now = LocalDateTime.now(clock);
        List<ReviewState> reviewStates = reviewStateRepository.findDueByDeckProgress(deckProgress, now, Pageable.ofSize(100));

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

        if (rating == ReviewRating.GOOD && nextLevel.isLastLevel()) {
            //TODO Graduate Word
        } else {
            reviewState.setLevel(nextLevel);

            LocalDateTime nextReviewAt = LocalDateTime.now(clock).plus(Srs.intervalForLevel(nextLevel));
            reviewState.setNextReviewAt(nextReviewAt);
        }

        // in-session behavior
        if (rating == ReviewRating.AGAIN) {
            reviewSession.getReviewQueue().addLast(reviewStateId);
        }

        reviewSession.setShowAnswer(false);
    }
}
