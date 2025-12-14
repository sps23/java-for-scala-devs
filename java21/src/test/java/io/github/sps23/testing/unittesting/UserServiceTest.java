package io.github.sps23.testing.unittesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;

/**
 * Comprehensive unit tests for UserService demonstrating best practices: -
 * Parallel test execution for better performance - Proper use of mocks with
 * Mockito - Test isolation (no shared state) - Clear test structure
 * with @BeforeEach setup - Descriptive test names and @DisplayName -
 * Verification of interactions with mocks
 */
@DisplayName("UserService Tests with Mockito")
@Execution(ExecutionMode.CONCURRENT) // Enable parallel execution
class UserServiceTest {

    private UserRepository userRepository;
    private EmailService emailService;
    private UserService userService;

    /**
     * Setup method that runs before each test. Creates fresh mocks for each test
     * ensuring test isolation.
     */
    @BeforeEach
    void setUp() {
        // Create fresh mocks for each test
        userRepository = mock(UserRepository.class);
        emailService = mock(EmailService.class);
        userService = new UserService(userRepository, emailService);
    }

    /**
     * Optional cleanup method. In this case, not needed as mocks are recreated for
     * each test, but useful for cleaning up resources like temporary files,
     * database connections, etc.
     */
    @AfterEach
    void tearDown() {
        // No cleanup needed for mocks, but this demonstrates the hook
        // In real scenarios: close connections, delete temp files, etc.
    }

    @Nested
    @DisplayName("User Creation Tests")
    @Execution(ExecutionMode.CONCURRENT)
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when all validations pass")
        void shouldCreateUserSuccessfully() {
            // Arrange
            String id = "user1";
            String name = "John Doe";
            String email = "john@example.com";
            User expectedUser = new User(id, name, email, true);

            when(userRepository.existsById(id)).thenReturn(false);
            when(emailService.isEmailValid(email)).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(expectedUser);

            // Act
            User result = userService.createUser(id, name, email);

            // Assert
            assertNotNull(result);
            assertEquals(id, result.id());
            assertEquals(name, result.name());
            assertEquals(email, result.email());
            assertTrue(result.active());

            // Verify interactions
            verify(userRepository).existsById(id);
            verify(emailService).isEmailValid(email);
            verify(userRepository).save(any(User.class));
            verify(emailService).sendEmail(eq(email), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when user ID already exists")
        void shouldThrowExceptionWhenUserExists() {
            // Arrange
            String id = "user1";
            when(userRepository.existsById(id)).thenReturn(true);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(id, "John", "john@example.com"));

            // Verify no other interactions occurred
            verify(userRepository).existsById(id);
            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailInvalid() {
            // Arrange
            String id = "user1";
            String email = "invalid-email";
            when(userRepository.existsById(id)).thenReturn(false);
            when(emailService.isEmailValid(email)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(id, "John", email));

            verify(userRepository).existsById(id);
            verify(emailService).isEmailValid(email);
            verify(userRepository, never()).save(any());
            verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should send welcome email with correct content")
        void shouldSendWelcomeEmailWithCorrectContent() {
            // Arrange
            String id = "user1";
            String name = "John Doe";
            String email = "john@example.com";
            User user = new User(id, name, email, true);

            when(userRepository.existsById(id)).thenReturn(false);
            when(emailService.isEmailValid(email)).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Create ArgumentCaptor to capture method arguments
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            // Act
            userService.createUser(id, name, email);

            // Assert - verify email was sent with correct arguments
            verify(emailService).sendEmail(emailCaptor.capture(), subjectCaptor.capture(),
                    bodyCaptor.capture());

            assertEquals(email, emailCaptor.getValue());
            assertEquals("Welcome!", subjectCaptor.getValue());
            assertTrue(bodyCaptor.getValue().contains(name));
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    @Execution(ExecutionMode.CONCURRENT)
    class GetUserTests {

        @Test
        @DisplayName("Should return user when user exists")
        void shouldReturnUserWhenExists() {
            // Arrange
            String id = "user1";
            User expectedUser = new User(id, "John Doe", "john@example.com", true);
            when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

            // Act
            Optional<User> result = userService.getUserById(id);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(expectedUser, result.get());
            verify(userRepository).findById(id);
        }

        @Test
        @DisplayName("Should return empty optional when user does not exist")
        void shouldReturnEmptyWhenUserNotExists() {
            // Arrange
            String id = "nonexistent";
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // Act
            Optional<User> result = userService.getUserById(id);

            // Assert
            assertTrue(result.isEmpty());
            verify(userRepository).findById(id);
        }

        @Test
        @DisplayName("Should return only active users")
        void shouldReturnOnlyActiveUsers() {
            // Arrange
            List<User> allUsers = List.of(new User("1", "Active1", "active1@example.com", true),
                    new User("2", "Inactive", "inactive@example.com", false),
                    new User("3", "Active2", "active2@example.com", true));

            when(userRepository.findAll()).thenReturn(allUsers);

            // Act
            List<User> activeUsers = userService.getAllActiveUsers();

            // Assert
            assertEquals(2, activeUsers.size());
            assertTrue(activeUsers.stream().allMatch(User::active));
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("User Deactivation Tests")
    @Execution(ExecutionMode.CONCURRENT)
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user and send notification email")
        void shouldDeactivateUserSuccessfully() {
            // Arrange
            String id = "user1";
            User activeUser = new User(id, "John Doe", "john@example.com", true);
            User deactivatedUser = activeUser.withActive(false);

            when(userRepository.findById(id)).thenReturn(Optional.of(activeUser));
            when(userRepository.save(any(User.class))).thenReturn(deactivatedUser);

            // Act
            userService.deactivateUser(id);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertFalse(userCaptor.getValue().active());

            verify(emailService).sendEmail(eq(activeUser.email()), eq("Account Deactivated"),
                    contains(activeUser.name()));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            String id = "nonexistent";
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> userService.deactivateUser(id));

            verify(userRepository).findById(id);
            verify(userRepository, never()).save(any());
            verifyNoInteractions(emailService);
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    @Execution(ExecutionMode.CONCURRENT)
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user when user exists")
        void shouldDeleteUserSuccessfully() {
            // Arrange
            String id = "user1";
            when(userRepository.existsById(id)).thenReturn(true);

            // Act
            userService.deleteUser(id);

            // Assert
            verify(userRepository).existsById(id);
            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent user")
        void shouldThrowExceptionWhenUserNotExists() {
            // Arrange
            String id = "nonexistent";
            when(userRepository.existsById(id)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(id));

            verify(userRepository).existsById(id);
            verify(userRepository, never()).deleteById(anyString());
        }
    }
}
