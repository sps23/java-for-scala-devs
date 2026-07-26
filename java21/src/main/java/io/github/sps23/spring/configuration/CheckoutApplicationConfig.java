package io.github.sps23.spring.configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * Java-based configuration that mixes component scanning, explicit bean factory
 * methods, imported XML, and one Spring 7 registrar.
 */
@Configuration
@ComponentScan(basePackageClasses = CheckoutService.class)
@Import(Spring7FeatureRegistrar.class)
@ImportResource("classpath:io/github/sps23/spring/configuration/legacy-support-policy.xml")
public class CheckoutApplicationConfig {

    @Bean
    public Clock invoiceClock() {
        return Clock.fixed(Instant.parse("2026-07-26T09:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    public InvoiceFormatter invoiceFormatter(Clock invoiceClock) {
        return new InvoiceFormatter(invoiceClock, "EUR");
    }

    @Bean
    @Profile("dev")
    public PaymentClient sandboxPaymentClient() {
        return new SandboxPaymentClient("sandbox-terminal");
    }

    @Bean
    @Profile("prod")
    public PaymentClient livePaymentClient() {
        return new LivePaymentClient("merchant-live-42");
    }
}
