package io.github.sps23.testing.unittesting;

/**
 * Service interface for sending emails. This abstraction allows easy mocking in
 * tests without actually sending emails.
 */
public interface EmailService {
    void sendEmail(String to, String subject, String body);

    boolean isEmailValid(String email);
}
