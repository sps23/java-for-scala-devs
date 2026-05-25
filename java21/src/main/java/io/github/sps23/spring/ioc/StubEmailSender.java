package io.github.sps23.spring.ioc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test-double implementation of {@link EmailSender} that records sent emails
 * instead of actually delivering them.
 *
 * <p>
 * Use this in tests to assert that the right emails were sent without spinning
 * up an SMTP server or paying a SaaS provider for test traffic:
 *
 * <pre>{@code
 * var emailSender = new StubEmailSender();
 * var service = new OrderService(gateway, emailSender, repository);
 * service.placeOrder(order);
 * assertThat(emailSender.sentEmails()).hasSize(1);
 * assertThat(emailSender.sentEmails().get(0).to()).isEqualTo("customer@example.com");
 * }</pre>
 */
public class StubEmailSender implements EmailSender {

    /**
     * Captures a single sent email.
     */
    public record SentEmail(String to, String subject) {
    }

    private final List<SentEmail> sent = new ArrayList<>();

    @Override
    public void send(String to, String subject) {
        sent.add(new SentEmail(to, subject));
    }

    /**
     * Returns all emails "sent" via this stub — in order.
     *
     * @return unmodifiable list of sent emails
     */
    public List<SentEmail> sentEmails() {
        return Collections.unmodifiableList(sent);
    }

    /**
     * Clears the sent-email history — handy in {@code @AfterEach} setup.
     */
    public void reset() {
        sent.clear();
    }
}
