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

        var ids = dueWords.stream().map(Word::getId).toList();
        session.setQueue(new ArrayDeque<>(ids));

        return session;
    }

    public void applyRating(ReviewSession session, ReviewRating rating) {
        Long currentId = session.getQueue().pollFirst(); // remove current
        if (currentId == null) return;

        if (rating == ReviewRating.AGAIN) {
            session.getQueue().addLast(currentId); // reinsert at end
        }

        session.setShowAnswer(false);
    }
}
