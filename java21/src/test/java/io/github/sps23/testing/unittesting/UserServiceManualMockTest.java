package io.github.sps23.testing.unittesting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Tests using hand-rolled mocks instead of Mockito. This demonstrates: - When
 * manual mocks make sense (simple interfaces, learning purposes) - How to
 * create simple mock implementations - Pros: No external dependencies, full
 * control, easier debugging - Cons: More boilerplate, manual verification
 * tracking, harder to maintain
 */
@DisplayName("UserService Tests with Manual Mocks")
@Execution(ExecutionMode.CONCURRENT)
class UserServiceManualMockTest {

    private FakeUserRepository userRepository;
    private FakeEmailService emailService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        emailService = new FakeEmailService();
        userService = new UserService(userRepository, emailService);
    }

    @Test
    @DisplayName("Should create user and verify all interactions")
    void shouldCreateUserSuccessfully() {
        // Arrange
        String id = "user1";
        String name = "John Doe";
        String email = "john@example.com";

        // Act
        User result = userService.createUser(id, name, email);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.id());
        assertTrue(userRepository.savedUsers.containsKey(id));
        assertEquals(1, emailService.sentEmails.size());

        FakeEmailService.EmailRecord sentEmail = emailService.sentEmails.get(0);
        assertEquals(email, sentEmail.to);
        assertEquals("Welcome!", sentEmail.subject);
        assertTrue(sentEmail.body.contains(name));
    }

    @Test
    @DisplayName("Should retrieve existing user")
    void shouldGetUserById() {
        // Arrange
        User user = new User("user1", "John", "john@example.com", true);
        userRepository.users.put(user.id(), user);

        // Act
        Optional<User> result = userService.getUserById(user.id());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    @DisplayName("Should handle non-existent user")
    void shouldReturnEmptyForNonExistentUser() {
        // Act
        Optional<User> result = userService.getUserById("nonexistent");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter active users")
    void shouldGetOnlyActiveUsers() {
        // Arrange
        userRepository.users.put("1", new User("1", "Active", "active@example.com", true));
        userRepository.users.put("2", new User("2", "Inactive", "inactive@example.com", false));

        // Act
        List<User> activeUsers = userService.getAllActiveUsers();

        // Assert
        assertEquals(1, activeUsers.size());
        assertEquals("1", activeUsers.get(0).id());
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate user")
    void shouldThrowExceptionForDuplicateUser() {
        // Arrange
        String id = "user1";
        userRepository.users.put(id, new User(id, "Existing", "existing@example.com", true));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(id, "New", "new@example.com"));
    }

    /**
     * Fake implementation of UserRepository for testing. Uses in-memory
     * ConcurrentHashMap for thread-safe parallel test execution.
     */
    static class FakeUserRepository implements UserRepository {
        final Map<String, User> users = new ConcurrentHashMap<>();
        final Map<String, User> savedUsers = new ConcurrentHashMap<>();

        @Override
        public Optional<User> findById(String id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }

        @Override
        public User save(User user) {
            users.put(user.id(), user);
            savedUsers.put(user.id(), user);
            return user;
        }

        @Override
        public void deleteById(String id) {
            users.remove(id);
        }

        @Override
        public boolean existsById(String id) {
            return users.containsKey(id);
        }
    }

    /**
     * Fake implementation of EmailService for testing. Records all sent emails for
     * verification. Thread-safe for parallel test execution.
     */
    static class FakeEmailService implements EmailService {
        final List<EmailRecord> sentEmails = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void sendEmail(String to, String subject, String body) {
            sentEmails.add(new EmailRecord(to, subject, body));
        }

        @Override
        public boolean isEmailValid(String email) {
            return email != null && email.contains("@");
        }

        record EmailRecord(String to, String subject, String body) {
        }
    }
}
