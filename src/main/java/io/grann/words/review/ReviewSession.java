package io.grann.words.review;

import lombok.Getter;
import lombok.Setter;

import java.util.Deque;

@Getter
@Setter
public class ReviewSession {
    private Deque<Long> queue;
    private boolean showAnswer = false;
    private int totalCount = 0;

    public Long getCurrentWordId() {
        return queue.peekFirst(); // does NOT remove
    }

    public int getRemainingCount() {
        return queue.size();
    }

    public int getCompletedCount() {
        return totalCount - getRemainingCount();
    }

    public boolean isFinished() {
        return queue.isEmpty();
    }
}
