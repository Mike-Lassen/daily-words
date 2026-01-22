package io.grann.words.domain;

import java.time.Duration;

public enum SrsLevel {

    LEVEL_1(1, false),
    LEVEL_2(2, false),
    LEVEL_3(5, false),
    LEVEL_4(12, true),
    LEVEL_5(28, true),
    LEVEL_6(63, true);

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
