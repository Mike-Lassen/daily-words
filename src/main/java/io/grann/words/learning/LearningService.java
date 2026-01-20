package io.grann.words.learning;

import io.grann.words.domain.Word;
import io.grann.words.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningService {

    private final WordRepository wordRepository;
    private final Clock clock;

    public LearningSession startSession() {
        List<Word> words =
                wordRepository.findTop5ByStatusOrderByIdAsc(io.grann.words.domain.WordStatus.LEARNING);

        if (words.size() < 5) {
            throw new IllegalStateException("Not enough new words to learn");
        }

        for (Word word : words) {
            String kana = word.getKana();
        }

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
        for (Word word : session.getWords()) {
            word.enterReviewing(clock); // sets status + ensures reviewState exists
        }
        wordRepository.saveAll(session.getWords()); // merge + cascade
    }
}
