package io.grann.words.review;

import io.grann.words.domain.ReviewState;
import io.grann.words.domain.SrsLevel;
import io.grann.words.domain.Word;
import io.grann.words.domain.WordStatus;
import io.grann.words.repository.ReviewStateRepository;
import io.grann.words.repository.WordRepository;
import io.grann.words.srs.Srs;
import lombok.RequiredArgsConstructor;
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
    private final Clock clock;

    public ReviewSession startSession() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Word> dueWords = wordRepository.findWordsDueForReview(now);

        ReviewSession session = new ReviewSession();
        session.setTotalCount(dueWords.size());

        var ids = dueWords.stream().map(Word::getId).toList();
        session.setQueue(new ArrayDeque<>(ids));

        return session;
    }

    @Transactional
    public void applyRating(ReviewSession reviewSession, ReviewRating rating) {
        Long wordIdentifier = reviewSession.getQueue().pollFirst();
        if (wordIdentifier == null) {
            return;
        }

        Word word = wordRepository.findById(wordIdentifier).orElseThrow();

        ReviewState reviewState = reviewStateRepository.findByWord(word).get();

        SrsLevel currentLevel = reviewState.getLevel();
        SrsLevel nextLevel =
                (rating == ReviewRating.GOOD)
                        ? Srs.onGood(currentLevel)
                        : Srs.onAgain(currentLevel);

        if (rating == ReviewRating.GOOD && nextLevel.isLastLevel()) {
            word.setStatus(WordStatus.GRADUATED);
        } else {
            reviewState.setLevel(nextLevel);

            LocalDateTime nextReviewAt = LocalDateTime.now(clock).plus(Srs.intervalForLevel(nextLevel));
            reviewState.setNextReviewAt(nextReviewAt);
        }

        // in-session behavior
        if (rating == ReviewRating.AGAIN) {
            reviewSession.getQueue().addLast(wordIdentifier);
        }

        reviewSession.setShowAnswer(false);
    }
}
