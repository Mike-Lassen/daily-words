package io.grann.words.config;

import io.grann.words.bootstrap.AdjustableClock;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.time.LocalDateTime;

@Configuration
@Slf4j
public class DailyWordsConfig {

    @Bean
    Clock defaultClock(Environment environment) {
        boolean clockEnabled = Boolean.parseBoolean(
                environment.getProperty("app.clock.enabled", "false")
        );
        if (clockEnabled) {
            String value = environment.getProperty("app.clock.value", "now");
            if (!value.equals("now")) {
                LocalDateTime localDateTime = LocalDateTime.parse(value);
                log.info("pretending the date and time is: " + localDateTime);
                return new AdjustableClock(localDateTime);
            }
        }
        return Clock.systemDefaultZone();
    }


}
