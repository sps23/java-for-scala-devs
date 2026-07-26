package io.github.sps23.spring.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests for the Spring configuration examples used by the blog post.
 */
@DisplayName("Spring configuration approaches")
class CheckoutApplicationConfigTest {

    @Test
    @DisplayName("Java config should combine component scanning, @Bean methods, and imported XML")
    void javaConfigShouldCombineComponentScanningBeanMethodsAndImportedXml() {
        try (var context = newContext("dev")) {
            var service = context.getBean(CheckoutService.class);

            var receipt = service.checkout(
                    new CheckoutRequest("cust-1", "PL", new BigDecimal("100.00"), "partner"));

            assertEquals("sandbox", receipt.paymentMode());
            assertEquals(new BigDecimal("123.00"), receipt.total());
            assertEquals("xml-partner-ops", receipt.supportQueue());
            assertTrue(receipt.transactionId().startsWith("sandbox-cust-1-"));
            assertTrue(receipt.invoiceLine().contains("EUR"));
            assertTrue(receipt.invoiceLine().contains("2026-07-26T09:00:00Z"));
            assertNotNull(context.getBean(TaxCalculator.class));
            assertNotNull(context.getBean(InvoiceFormatter.class));
        }
    }

    @Test
    @DisplayName("Profiles should swap the active payment configuration")
    void profilesShouldSwapTheActivePaymentConfiguration() {
        try (var devContext = newContext("dev"); var prodContext = newContext("prod")) {
            var request = new CheckoutRequest("cust-2", "DE", new BigDecimal("100.00"), "web");

            var devReceipt = devContext.getBean(CheckoutService.class).checkout(request);
            var prodReceipt = prodContext.getBean(CheckoutService.class).checkout(request);

            assertEquals("sandbox", devReceipt.paymentMode());
            assertTrue(devReceipt.transactionId().contains("sandbox-terminal"));
            assertEquals("live", prodReceipt.paymentMode());
            assertTrue(prodReceipt.transactionId().contains("merchant-live-42"));
            assertEquals(new BigDecimal("119.00"), devReceipt.total());
            assertEquals(devReceipt.total(), prodReceipt.total());
        }
    }

    @Test
    @DisplayName("Spring 7 BeanRegistrar should register a bean programmatically")
    void spring7BeanRegistrarShouldRegisterABeanProgrammatically() {
        try (var context = newContext("prod")) {
            var note = context.getBean(Spring7FeatureNote.class);

            assertTrue(note.summary().contains("BeanRegistrar"));
            assertTrue(note.summary().contains("production"));
        }
    }

    @Test
    @DisplayName("Legacy XML-only configuration should still wire the checkout flow")
    void legacyXmlOnlyConfigurationShouldStillWireTheCheckoutFlow() {
        try (var context = new ClassPathXmlApplicationContext(
                "io/github/sps23/spring/configuration/legacy-checkout-context.xml")) {
            var service = context.getBean(CheckoutService.class);

            var receipt = service
                    .checkout(new CheckoutRequest("cust-3", "DE", new BigDecimal("100.00"), "web"));

            assertEquals("sandbox", receipt.paymentMode());
            assertEquals(new BigDecimal("119.00"), receipt.total());
            assertEquals("xml-general-support", receipt.supportQueue());
            assertTrue(receipt.transactionId().contains("xml-terminal"));
            assertTrue(receipt.invoiceLine().contains("EUR"));
        }
    }

    private AnnotationConfigApplicationContext newContext(String... profiles) {
        var context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles(profiles);
        context.register(CheckoutApplicationConfig.class);
        context.refresh();
        return context;
    }
}
