package io.baxter.authentication.infrastructure.behavior.helper;

import lombok.Generated;
import org.springframework.context.annotation.*;

import java.time.Clock;

@Generated
@Configuration
public class DateTimeConfig {
    @Bean
    public Clock clock(){
        return Clock.systemUTC();
    }
}
