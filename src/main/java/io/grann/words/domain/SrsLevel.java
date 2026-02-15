package io.grann.words.domain;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public enum SrsLevel {

    //
    // Should be
    // 1: 1
    // 2: 2
    // 3: 5
    // 4: 12
    // 5: 28
    // 6: 63
    //
    LEVEL_1(1, false),
    LEVEL_2(2, false),
    LEVEL_3(3, true),
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

    public static List<SrsLevel> getTraineeLevels() {
        return Arrays.stream(values())
                .filter(SrsLevel::isTrainee)
                .toList();
    }
    public static List<SrsLevel> getExpertLevels() {
        return Arrays.stream(values())
                .filter(SrsLevel::isExpert)
                .toList();
    }

}
