package io.grann.words.review;

import io.grann.words.domain.Word;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;

@Getter
@Setter
public class ReviewSession {
    private Deque<Word> reviewQueue;
    private Word currentWord;
    private boolean showAnswer = false;
    private int totalCount = 0;

    public int getRemainingCount() {
        return reviewQueue.size();
    }
    public int getCompletedCount() {
        return totalCount - getRemainingCount();
    }
    public boolean isFinished() {
        return reviewQueue.isEmpty();
    }
}
