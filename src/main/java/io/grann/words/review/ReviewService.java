package io.grann.words.review;
import io.grann.words.domain.Word;
import io.grann.words.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final WordRepository wordRepository;
    private final Clock clock;
    public ReviewSession startSession() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Word> dueWords = wordRepository.findWordsDueForReview(now);

        ReviewSession session = new ReviewSession();
        session.setTotalCount(dueWords.size());
        session.setReviewQueue(new ArrayDeque<>(dueWords));

        advance(session);
        return session;
    }

    public void applyRating(ReviewSession session, ReviewRating rating) {
        Word word = session.getCurrentWord();
        if (rating == ReviewRating.AGAIN) {
            session.getReviewQueue().addLast(word);
        }
        // GOOD -> discard
        session.setShowAnswer(false);
    }

    public void advance(ReviewSession session) {
        if (!session.getReviewQueue().isEmpty()) {
            session.setCurrentWord(session.getReviewQueue().pollFirst());
        }
    }
}