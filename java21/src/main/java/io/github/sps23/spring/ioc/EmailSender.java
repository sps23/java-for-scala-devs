package io.github.sps23.spring.ioc;

/**
 * Abstraction over an email delivery provider.
 *
 * <p>
 * Inject this interface into services that need to send email. In production
 * the container wires in an SMTP or SendGrid implementation; in tests you
 * inject a {@link StubEmailSender} and assert on what was "sent".
 */
public interface EmailSender {

    /**
     * Sends an email.
     *
     * @param to
     *            recipient email address
     * @param subject
     *            email subject line
     */
    void send(String to, String subject);
}
