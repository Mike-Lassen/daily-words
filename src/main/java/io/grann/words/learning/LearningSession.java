package io.grann.words.learning;

import io.grann.words.domain.Word;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class LearningSession {

    private List<Word> words;

    // Phase control
    private LearningPhase phase = LearningPhase.INTRODUCTION;

    // INTRODUCTION phase
    private int introIndex = 0;

    // REVIEW phase
    private Word currentWord;
    private boolean showAnswer = false;
    private Set<Long> passedWordIds = new HashSet<>();

    // ---------- Convenience ----------

    public Word getIntroWord() {
        return words.get(introIndex);
    }

    public boolean isLastIntroWord() {
        return introIndex == words.size() - 1;
    }

    public int getPassedCount() {
        return passedWordIds.size();
    }

    public int getTotalCount() {
        return words.size();
    }

    public boolean isFinished() {
        return passedWordIds.size() == words.size();
    }
}
