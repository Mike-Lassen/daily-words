package io.grann.words.bootstrap;

import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Setter
@Getter
public class AdjustableClock extends Clock {
    private LocalDateTime localDateTime;

    public AdjustableClock(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }
}
