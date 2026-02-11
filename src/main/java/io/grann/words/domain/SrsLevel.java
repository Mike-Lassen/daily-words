package io.grann.words.domain;

import java.time.Duration;

public enum SrsLevel {

    //
    // Should be
    // 1: 1
    // 2: 2
    // 3: 5
    // 4: 12
    // 5: 28
    // 6: 63
    // 7: 120
    //
    LEVEL_1(1, false),
    LEVEL_2(2, false),
    LEVEL_3(3, false),
    LEVEL_4(4, true),
    LEVEL_5(5, true),
    LEVEL_6(6, true);

    private final int intervalDays;
    private final boolean expert;

    SrsLevel(int intervalDays, boolean expert) {
        this.intervalDays = intervalDays;
        this.expert = expert;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public boolean isExpert() {
        return expert;
    }


    public SrsLevel next() {
        if (this == LEVEL_6) {
            return this;
        }
        return values()[ordinal() + 1];
    }
    public SrsLevel previous() {
        if (this == LEVEL_1) {
            return this;
        }
        return values()[ordinal() - 1];
    }
    public boolean isTrainee(){
        return !this.expert;
    }
    public boolean isLastLevel() {
        return this == LEVEL_6;
    }
    public Duration getInterval() {
        return Duration.ofDays(intervalDays);
    }
}
