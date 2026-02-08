package io.grann.words.review;

import lombok.Getter;
import lombok.Setter;

import java.util.Deque;

@Getter
@Setter
public class ReviewSession {
    /**
     * Session-local queue of ReviewState IDs.
     *
     * <p>Created when a review session starts and consumed sequentially
     * as the user completes reviews. This queue is mutated during the session
     * and discarded when the session ends.
     */
    private Deque<Long> reviewQueue;
    private boolean showAnswer = false;
    private int totalCount = 0;

    public Long getCurrentReviewStateId() {
        return reviewQueue.peekFirst();
    }

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
