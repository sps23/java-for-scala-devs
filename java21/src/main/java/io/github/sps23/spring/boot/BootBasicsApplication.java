package io.github.sps23.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot entry point used by the blog post example.
 */
@SpringBootApplication
public class BootBasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootBasicsApplication.class, args);
    }
}
