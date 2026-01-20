package io.grann.words.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DailyWordsConfig {
    @Bean
    Clock defaultClock() {
        return Clock.systemDefaultZone();
    }
}
