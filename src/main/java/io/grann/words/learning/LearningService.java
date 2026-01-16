package io.grann.words.learning;

import io.grann.words.domain.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LearningService {

    // Later this will query the database
    public LearningSession startSession(List<Word> newWords) {
        LearningSession session = new LearningSession();
        session.setWords(newWords);
        return session;
    }

    public void startReview(LearningSession session) {
        session.setPhase(LearningPhase.REVIEW);
        session.setPassedWordIds(new java.util.HashSet<>());
        nextReviewWord(session);
    }

    public void applyRating(LearningSession session, ReviewRating rating) {
        if (rating == ReviewRating.GOOD) {
            session.getPassedWordIds().add(session.getCurrentWord().getId());
        }

        session.setShowAnswer(false);

        if (!session.isFinished()) {
            nextReviewWord(session);
        }
    }

    private void nextReviewWord(LearningSession session) {
        List<Word> remaining = session.getWords().stream()
                .filter(w -> !session.getPassedWordIds().contains(w.getId()))
                .toList();

        session.setCurrentWord(
                remaining.get(new Random().nextInt(remaining.size()))
        );
    }
}
