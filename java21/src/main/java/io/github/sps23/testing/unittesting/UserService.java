package io.github.sps23.testing.unittesting;

import java.util.List;
import java.util.Optional;

/**
 * User service with proper dependency injection for testability. All
 * dependencies are injected through constructor, making it easy to provide
 * mocks in tests.
 */
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User createUser(String id, String name, String email) {
        if (userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " already exists");
        }

        if (!emailService.isEmailValid(email)) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }

        var user = new User(id, name, email, true);
        var savedUser = userRepository.save(user);

        emailService.sendEmail(email, "Welcome!", "Welcome to our service, " + name + "!");

        return savedUser;
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findAll().stream().filter(User::active).toList();
    }

    public void deactivateUser(String id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        var deactivatedUser = user.withActive(false);
        userRepository.save(deactivatedUser);

        emailService.sendEmail(user.email(), "Account Deactivated",
                "Your account has been deactivated, " + user.name());
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
