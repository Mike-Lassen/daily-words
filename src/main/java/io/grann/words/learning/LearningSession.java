package io.grann.words.learning;

import io.grann.words.domain.Word;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Getter
@Setter
public class LearningSession {

    private List<Word> words;

    // Phase control
    private LearningPhase phase = LearningPhase.INTRODUCTION;

    // INTRODUCTION
    private int introIndex = 0;

    // REVIEW
    private Deque<Word> reviewQueue;
    private Word currentWord;
    private boolean showAnswer = false;

    // ---------- Convenience ----------

    public Word getIntroWord() {
        return words.get(introIndex);
    }

    public boolean isLastIntroWord() {
        return introIndex == words.size() - 1;
    }

    public int getTotalCount() {
        return words.size();
    }

    public int getRemainingCount() {
        return reviewQueue.size();
    }

    public boolean isFinished() {
        return reviewQueue.isEmpty();
    }
}
