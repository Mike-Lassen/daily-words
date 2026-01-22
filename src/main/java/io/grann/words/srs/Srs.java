package io.grann.words.srs;

import io.grann.words.domain.SrsLevel;

import java.time.Duration;

public final class Srs {

    private Srs() {
    }

    public static SrsLevel onGood(SrsLevel currentLevel) {
        if (currentLevel.isLastLevel()) {
            return currentLevel;
        }
        return currentLevel.next();
    }

    public static SrsLevel onAgain(SrsLevel currentLevel) {
        if (currentLevel.isTrainee()) {
            return currentLevel.previous();
        }
        // expert levels drop two
        return currentLevel.previous().previous();
    }

    public static Duration intervalForLevel(SrsLevel level) {
        return level.getInterval();
    }
}
