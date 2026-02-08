package io.grann.words.learning;

import io.grann.words.domain.DeckProgress;
import io.grann.words.domain.Word;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Getter
@Setter
public class LearningSession {
    /**
     * Session-local list of Word IDs.
     *
     * <p>Created when a learning session starts
     */
    private List<Long> words;

    // Phase control
    private LearningPhase phase = LearningPhase.INTRODUCTION;

    // INTRODUCTION
    private int introIndex = 0;

    // REVIEW
    private Deque<Word> reviewQueue;
    private Word currentWord;
    private boolean showAnswer = false;

    // ---------- Introduction ----------

    public long getIntroWordId() {
        return words.get(introIndex);
    }

    public boolean isLastIntroWord() {
        return introIndex == words.size() - 1;
    }

    public int getTotalCount() {
        return words.size();
    }

    // ---------- Review ----------

    public int getRemainingCount() {
        return reviewQueue.size();
    }

    public boolean isFinished() {
        return reviewQueue.isEmpty();
    }
}
