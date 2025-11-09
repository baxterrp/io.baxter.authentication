package io.baxter.authentication.infrastructure.behavior.helper;

import org.springframework.context.annotation.*;

import java.time.Clock;

@Configuration
public class DateTimeConfig {
    @Bean
    public Clock clock(){
        return Clock.systemUTC();
    }
}
