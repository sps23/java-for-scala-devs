package io.github.sps23.spring.boot;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST endpoint that reads its message from configuration.
 */
@RestController
@RequestMapping("/api/greeting")
public class GreetingController {

    private final AppProperties properties;

    public GreetingController(AppProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public Map<String, String> greeting() {
        return Map.of("message", properties.greeting());
    }
}
