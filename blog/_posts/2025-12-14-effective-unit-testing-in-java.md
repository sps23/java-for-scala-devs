---
layout: post
title: "Effective Unit Testing in Java 21 with JUnit 5"
description: "Master unit testing with JUnit 5: parallel execution, test isolation, testable service design, setup/teardown, mocking strategies, best practices, and common anti-patterns to avoid."
date: 2025-12-14 18:00:00 +0000
categories: [testing, best-practices]
tags: [java, junit5, mockito, testing, unit-testing, parallel-execution, test-isolation, best-practices, interview-preparation]
---

Writing effective unit tests is crucial for maintaining high-quality software. This comprehensive guide covers everything you need to know about unit testing in Java 21 with JUnit 5, including parallel execution, test isolation, testable service design, mocking strategies, and common pitfalls to avoid.

## Table of Contents

1. [The Fundamentals of Good Unit Tests](#fundamentals)
2. [Designing Services for Testability](#testable-design)
3. [Test Setup and Teardown](#setup-teardown)
4. [Parallel Test Execution](#parallel-execution)
5. [Mocking: Mockito vs. Hand-Rolled Mocks](#mocking-strategies)
6. [Best Practices](#best-practices)
7. [Common Anti-Patterns](#anti-patterns)

## <a name="fundamentals"></a>The Fundamentals of Good Unit Tests

Good unit tests should be **FIRST**:
- **Fast**: Execute quickly (milliseconds, not seconds)
- **Independent**: No dependencies between tests
- **Repeatable**: Same result every time
- **Self-validating**: Pass or fail, no manual checking
- **Timely**: Written alongside or before the code

### Key Principles

```java
// ✅ GOOD: Fast, isolated, independent
@Test
void shouldCalculateSum() {
    Calculator calc = new Calculator();
    assertEquals(5, calc.add(2, 3));
}

// ❌ BAD: Slow, external dependency
@Test
void shouldSaveToDatabase() {
    Connection conn = DriverManager.getConnection("jdbc:postgresql://...");
    // Slow database I/O, requires real database
}
```

## <a name="testable-design"></a>Designing Services for Testability

The most important factor for testable code is **dependency injection** and **programming to interfaces**.

### Anti-Pattern: Hard-Coded Dependencies

```java
public class BadUserService {
    // ❌ Hard-coded database connection - impossible to test
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mydb";
    
    public User createUser(String id, String name, String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL, "admin", "password")) {
            // SQL logic mixed with business logic
            String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
            // ... execute SQL ...
            
            // ❌ Hard-coded email sending - will send real emails in tests!
            sendEmailDirectly(email, "Welcome!", "Welcome!");
            
            return new User(id, name, email, true);
        }
    }
    
    private void sendEmailDirectly(String to, String subject, String body) {
        // SMTP logic here - actually sends emails!
    }
}
```

**Problems with this design:**
1. Cannot test without a real database
2. Will send real emails during tests
3. Slow (network I/O)
4. Fragile (depends on external services)
5. Difficult to verify behavior
6. Impossible to run in parallel

### Best Practice: Dependency Injection with Interfaces

```java
// Domain model using immutable record
public record User(String id, String name, String email, boolean active) {
    public User {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }
    
    public User withActive(boolean active) {
        return new User(id, name, email, active);
    }
}

// Repository interface for data access
public interface UserRepository {
    Optional<User> findById(String id);
    List<User> findAll();
    User save(User user);
    void deleteById(String id);
    boolean existsById(String id);
}

// Email service interface
public interface EmailService {
    void sendEmail(String to, String subject, String body);
    boolean isEmailValid(String email);
}

// ✅ GOOD: Testable service with dependency injection
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // Constructor injection - easy to provide mocks in tests
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
    
    public void deactivateUser(String id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        var deactivatedUser = user.withActive(false);
        userRepository.save(deactivatedUser);
        
        emailService.sendEmail(user.email(), "Account Deactivated", 
            "Your account has been deactivated, " + user.name());
    }
}
```

**Benefits of this design:**
1. ✅ Easy to test with mocks
2. ✅ No external dependencies in tests
3. ✅ Fast execution
4. ✅ Can verify all interactions
5. ✅ Thread-safe and parallelizable
6. ✅ Follows SOLID principles

## <a name="setup-teardown"></a>Test Setup and Teardown

JUnit 5 provides lifecycle hooks to set up and clean up test resources.

### Lifecycle Annotations

```java
@DisplayName("UserService Tests")
class UserServiceTest {
    
    private UserRepository userRepository;
    private EmailService emailService;
    private UserService userService;
    
    @BeforeAll
    static void setupAll() {
        // Runs once before all tests in this class
        // Use for expensive setup shared by all tests
        // Example: Starting test containers, loading config
        System.out.println("Setting up test suite");
    }
    
    @BeforeEach
    void setUp() {
        // ✅ Runs before EACH test - ensures test isolation
        // Create fresh mocks for each test
        userRepository = mock(UserRepository.class);
        emailService = mock(EmailService.class);
        userService = new UserService(userRepository, emailService);
    }
    
    @Test
    void shouldCreateUser() {
        // Arrange
        when(userRepository.existsById("1")).thenReturn(false);
        when(emailService.isEmailValid("john@example.com")).thenReturn(true);
        when(userRepository.save(any(User.class)))
            .thenReturn(new User("1", "John", "john@example.com", true));
        
        // Act
        User result = userService.createUser("1", "John", "john@example.com");
        
        // Assert
        assertNotNull(result);
        verify(emailService).sendEmail(eq("john@example.com"), anyString(), anyString());
    }
    
    @AfterEach
    void tearDown() {
        // Runs after each test
        // Clean up resources: close connections, delete temp files, etc.
        // For mocks, usually not needed (they're recreated in @BeforeEach)
    }
    
    @AfterAll
    static void tearDownAll() {
        // Runs once after all tests
        // Clean up expensive resources
        // Example: Stopping test containers
        System.out.println("Tearing down test suite");
    }
}
```

### When to Use Each Hook

| Hook | Use Case | Example |
|------|----------|---------|
| `@BeforeAll` | One-time expensive setup | Start test database container |
| `@BeforeEach` | Per-test setup, fresh state | Create mocks, initialize objects |
| `@AfterEach` | Per-test cleanup | Close files, reset static state |
| `@AfterAll` | One-time cleanup | Stop containers, cleanup temp directories |

### Common Setup Mistakes

```java
// ❌ WRONG: Static mutable state
class BadTest {
    private static UserService service;  // Shared between tests!
    
    @BeforeAll
    static void setup() {
        service = new UserService(...);  // Created once, shared by all tests
    }
    
    @Test
    void test1() {
        service.createUser(...);  // Modifies shared state
    }
    
    @Test
    void test2() {
        // Will see side effects from test1 - NOT ISOLATED!
    }
}

// ✅ CORRECT: Fresh instances per test
class GoodTest {
    private UserService service;  // Instance field, not static
    
    @BeforeEach
    void setUp() {
        // New instance for each test
        var repo = mock(UserRepository.class);
        var email = mock(EmailService.class);
        service = new UserService(repo, email);
    }
    
    @Test
    void test1() {
        // Uses fresh service instance
    }
    
    @Test
    void test2() {
        // Uses different fresh service instance - ISOLATED!
    }
}
```

## <a name="parallel-execution"></a>Parallel Test Execution

Parallel execution dramatically improves test suite performance. JUnit 5 makes this easy.

### Enabling Parallel Execution

Create `src/test/resources/junit-platform.properties`:

```properties
# Enable parallel execution
junit.jupiter.execution.parallel.enabled=true

# Run test classes and methods concurrently
junit.jupiter.execution.parallel.mode.default=concurrent

# Dynamic parallelism based on CPU cores
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=1.0

# Or use fixed thread count
# junit.jupiter.execution.parallel.config.strategy=fixed
# junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

### Making Tests Parallel-Safe

```java
@DisplayName("Parallel-Safe Tests")
@Execution(ExecutionMode.CONCURRENT)  // Enable for this class
class ParallelSafeTest {
    
    @BeforeEach
    void setUp() {
        // ✅ Fresh mocks for each test - thread-safe
    }
    
    @Test
    void independentTest1() {
        // ✅ Uses only local variables and method parameters
        String data = "test-data-" + System.currentTimeMillis();
        assertNotNull(data);
    }
    
    @Test
    void independentTest2() {
        // ✅ No shared state with other tests
        var service = new UserService(mock(UserRepository.class), mock(EmailService.class));
        assertNotNull(service);
    }
}

// When you MUST run sequentially (rare!)
@Execution(ExecutionMode.SAME_THREAD)
class SequentialTest {
    private int counter = 0;  // Shared mutable state
    
    @Test
    void test1() {
        counter = 1;
        assertEquals(1, counter);
    }
    
    @Test
    void test2() {
        counter = 2;
        assertEquals(2, counter);
    }
}
```

### Performance Comparison

```java
// Without parallel execution:
// 10 tests × 100ms each = 1000ms total

// With parallel execution (4 cores):
// 10 tests × 100ms each ÷ 4 cores ≈ 250ms total
// 4x speedup!
```

### Thread-Safety Best Practices

```java
class ThreadSafeTest {
    
    @Test
    void goodExample() {
        // ✅ Local variables - thread-safe
        var repository = new FakeRepository();
        var service = new UserService(repository, new FakeEmailService());
        
        // ✅ No shared mutable state
    }
}

// Manual mocks for parallel tests - use thread-safe collections
static class FakeRepository implements UserRepository {
    // ✅ ConcurrentHashMap is thread-safe
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    @Override
    public User save(User user) {
        users.put(user.id(), user);
        return user;
    }
    
    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }
}
```

## <a name="mocking-strategies"></a>Mocking: Mockito vs. Hand-Rolled Mocks

### When to Use Mockito

**Use Mockito when:**
- Testing complex interactions
- Need to verify method calls and arguments
- Working with multiple dependencies
- Team already uses Mockito
- Need advanced features (spies, argument captors, etc.)

### Mockito Example

```java
@DisplayName("UserService with Mockito")
class UserServiceMockitoTest {
    
    private UserRepository userRepository;
    private EmailService emailService;
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emailService = mock(EmailService.class);
        userService = new UserService(userRepository, emailService);
    }
    
    @Test
    @DisplayName("Should create user and send welcome email")
    void shouldCreateUserAndSendEmail() {
        // Arrange - define mock behavior
        when(userRepository.existsById("1")).thenReturn(false);
        when(emailService.isEmailValid("john@example.com")).thenReturn(true);
        when(userRepository.save(any(User.class)))
            .thenReturn(new User("1", "John", "john@example.com", true));
        
        // Act
        User result = userService.createUser("1", "John", "john@example.com");
        
        // Assert - verify behavior
        assertNotNull(result);
        assertEquals("John", result.name());
        
        // Verify interactions with mocks
        verify(userRepository).existsById("1");
        verify(emailService).isEmailValid("john@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(eq("john@example.com"), anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should verify email content with ArgumentCaptor")
    void shouldVerifyEmailContent() {
        // Arrange
        when(userRepository.existsById("1")).thenReturn(false);
        when(emailService.isEmailValid("john@example.com")).thenReturn(true);
        when(userRepository.save(any(User.class)))
            .thenReturn(new User("1", "John", "john@example.com", true));
        
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        
        // Act
        userService.createUser("1", "John", "john@example.com");
        
        // Assert - capture and verify arguments
        verify(emailService).sendEmail(
            emailCaptor.capture(), 
            subjectCaptor.capture(), 
            bodyCaptor.capture()
        );
        
        assertEquals("john@example.com", emailCaptor.getValue());
        assertEquals("Welcome!", subjectCaptor.getValue());
        assertTrue(bodyCaptor.getValue().contains("John"));
    }
    
    @Test
    @DisplayName("Should throw exception when user exists")
    void shouldThrowExceptionWhenUserExists() {
        // Arrange
        when(userRepository.existsById("1")).thenReturn(true);
        
        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser("1", "John", "john@example.com")
        );
        
        // Verify no other interactions
        verify(userRepository).existsById("1");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(emailService);
    }
}
```

### When to Use Hand-Rolled Mocks

**Use manual mocks when:**
- Interface is very simple
- Need to test state changes
- Want to avoid external dependencies
- Learning or teaching purposes
- Need full control over mock behavior

### Hand-Rolled Mock Example

```java
@DisplayName("UserService with Manual Mocks")
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
    @DisplayName("Should create user and verify interactions")
    void shouldCreateUser() {
        // Act
        User result = userService.createUser("1", "John", "john@example.com");
        
        // Assert
        assertNotNull(result);
        
        // Verify repository interactions
        assertTrue(userRepository.savedUsers.containsKey("1"));
        assertEquals("John", userRepository.savedUsers.get("1").name());
        
        // Verify email was sent
        assertEquals(1, emailService.sentEmails.size());
        FakeEmailService.EmailRecord email = emailService.sentEmails.get(0);
        assertEquals("john@example.com", email.to());
        assertEquals("Welcome!", email.subject());
        assertTrue(email.body().contains("John"));
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
    
    // Simple fake repository implementation
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
    
    // Simple fake email service implementation
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
        
        record EmailRecord(String to, String subject, String body) {}
    }
}
```

### Mockito vs. Hand-Rolled: Comparison

| Aspect | Mockito | Hand-Rolled |
|--------|---------|-------------|
| **Setup effort** | Low (one line) | Medium (write fake class) |
| **Verification** | Rich API (verify, times, etc.) | Manual (check fields) |
| **Flexibility** | High (argument captors, spies) | Total control |
| **Learning curve** | Moderate | Low |
| **Dependencies** | Requires library | None |
| **Debugging** | Can be tricky | Very clear |
| **Best for** | Complex interactions | Simple interfaces |

## <a name="best-practices"></a>Best Practices

### 1. Use Descriptive Test Names

```java
// ❌ Bad
@Test
void test1() { }

// ✅ Good
@Test
@DisplayName("Should create user successfully when all validations pass")
void shouldCreateUserSuccessfully() { }

@Test
@DisplayName("Should throw IllegalArgumentException when user ID already exists")
void shouldThrowExceptionWhenUserExists() { }
```

### 2. Follow AAA Pattern (Arrange-Act-Assert)

```java
@Test
void shouldCalculateDiscount() {
    // Arrange - set up test data
    var calculator = new PriceCalculator();
    var price = new Money(100, Currency.USD);
    var discount = Percentage.of(20);
    
    // Act - execute the code under test
    var result = calculator.applyDiscount(price, discount);
    
    // Assert - verify the outcome
    assertEquals(new Money(80, Currency.USD), result);
}
```

### 3. Test One Thing Per Test

```java
// ❌ Bad - tests multiple things
@Test
void testUserOperations() {
    service.createUser(...);
    service.updateUser(...);
    service.deleteUser(...);
    service.findUser(...);
}

// ✅ Good - one test per operation
@Test
void shouldCreateUser() {
    service.createUser(...);
    // assert creation
}

@Test
void shouldUpdateUser() {
    service.updateUser(...);
    // assert update
}
```

### 4. Use Nested Test Classes

```java
@DisplayName("UserService")
class UserServiceTest {
    
    @Nested
    @DisplayName("User Creation")
    class CreateUserTests {
        @Test
        void shouldCreateValidUser() { }
        
        @Test
        void shouldRejectDuplicateUser() { }
        
        @Test
        void shouldValidateEmail() { }
    }
    
    @Nested
    @DisplayName("User Retrieval")
    class GetUserTests {
        @Test
        void shouldFindExistingUser() { }
        
        @Test
        void shouldReturnEmptyForNonExistent() { }
    }
}
```

### 5. Use Parameterized Tests

```java
@ParameterizedTest
@DisplayName("Should validate email addresses correctly")
@CsvSource({
    "john@example.com, true",
    "invalid-email, false",
    "test@, false",
    "@example.com, false",
    "user.name+tag@example.co.uk, true"
})
void shouldValidateEmail(String email, boolean expected) {
    assertEquals(expected, emailService.isEmailValid(email));
}
```

### 6. Create Fresh Mocks in @BeforeEach

```java
class UserServiceTest {
    private UserRepository repository;
    private UserService service;
    
    @BeforeEach
    void setUp() {
        // ✅ Fresh mocks for each test - ensures isolation
        repository = mock(UserRepository.class);
        service = new UserService(repository);
    }
}
```

### 7. Verify Only What Matters

```java
@Test
void shouldCreateUser() {
    // Arrange
    when(repository.save(any())).thenReturn(user);
    
    // Act
    service.createUser("1", "John", "john@example.com");
    
    // ✅ Verify important interactions
    verify(repository).save(any(User.class));
    
    // ❌ Don't over-verify
    // verify(repository, times(1)).save(any());  // Redundant
    // verify(repository, never()).delete(any()); // Not relevant
}
```

## <a name="anti-patterns"></a>Common Anti-Patterns and Performance Killers

### 1. Shared Mutable State

```java
// ❌ ANTI-PATTERN: Static mutable state
class BadTest {
    private static int counter = 0;  // Shared between tests!
    
    @Test
    void test1() {
        counter++;
        assertEquals(1, counter);  // Fails if test2 runs first
    }
    
    @Test
    void test2() {
        counter++;
        assertEquals(1, counter);  // Fails if test1 runs first
    }
}

// ✅ CORRECT: No shared state
class GoodTest {
    @Test
    void test1() {
        int counter = 1;  // Local variable
        assertEquals(1, counter);  // Always passes
    }
    
    @Test
    void test2() {
        int counter = 1;  // Different local variable
        assertEquals(1, counter);  // Always passes
    }
}
```

**Impact:** Flaky tests, fails in parallel execution, wasted debugging time.

### 2. Tests with External Dependencies

```java
// ❌ ANTI-PATTERN: Real database connection
@Test
void testWithRealDatabase() {
    Connection conn = DriverManager.getConnection("jdbc:postgresql://...");
    // PROBLEMS:
    // - Slow (network I/O)
    // - Requires database to be running
    // - Can't run in parallel (shared database)
    // - Fragile (network issues, credentials, etc.)
}

// ✅ CORRECT: Use mocks
@Test
void testWithMocks() {
    UserRepository repository = mock(UserRepository.class);
    when(repository.findById("1")).thenReturn(Optional.of(user));
    // Fast, isolated, reliable
}
```

**Impact:** 100x slower tests, cannot run in CI without complex setup, flaky failures.

### 3. Thread.sleep() for Timing

```java
// ❌ ANTI-PATTERN: Using sleep to wait
@Test
void testAsync() throws InterruptedException {
    service.startAsyncOperation();
    Thread.sleep(1000);  // Hope it's done by now?
    // PROBLEMS:
    // - Unreliable (might not be enough time)
    // - Wastes time (might finish faster)
    // - Slows down test suite
}

// ✅ CORRECT: Use proper synchronization or mocks
@Test
void testAsync() {
    CompletableFuture<Result> future = service.startAsyncOperation();
    Result result = future.join();  // Wait for actual completion
    assertEquals(expected, result);
}
```

**Impact:** Slow tests, occasional failures, wasted CI time.

### 4. Test Interdependence

```java
// ❌ ANTI-PATTERN: Tests depending on execution order
@Test
void test1_CreateUser() {
    service.createUser(user);
}

@Test
void test2_UpdateUser() {
    service.updateUser(user);  // Assumes user exists from test1!
}

// ✅ CORRECT: Each test is independent
@Test
void shouldCreateUser() {
    service.createUser(user);
    assertTrue(service.exists(user.id()));
}

@Test
void shouldUpdateUser() {
    service.createUser(user);  // Create in this test
    service.updateUser(user);   // Then update
    // Each test stands alone
}
```

**Impact:** Breaks in parallel execution, fragile test suite, hard to debug.

### 5. No Setup Method - Code Duplication

```java
// ❌ ANTI-PATTERN: Duplicated setup in every test
class BadTest {
    @Test
    void test1() {
        var repository = mock(UserRepository.class);
        var emailService = mock(EmailService.class);
        var service = new UserService(repository, emailService);
        // ... test logic ...
    }
    
    @Test
    void test2() {
        var repository = mock(UserRepository.class);  // Duplicated!
        var emailService = mock(EmailService.class);  // Duplicated!
        var service = new UserService(repository, emailService);  // Duplicated!
        // ... test logic ...
    }
}

// ✅ CORRECT: Use @BeforeEach for setup
class GoodTest {
    private UserRepository repository;
    private EmailService emailService;
    private UserService service;
    
    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        emailService = mock(EmailService.class);
        service = new UserService(repository, emailService);
    }
    
    @Test
    void test1() {
        // Clean test logic, no duplication
    }
    
    @Test
    void test2() {
        // Clean test logic, no duplication
    }
}
```

**Impact:** Code duplication, harder to maintain, violates DRY principle.

### 6. Testing Multiple Things in One Test

```java
// ❌ ANTI-PATTERN: Giant test method
@Test
void testEverything() {
    service.createUser(...);
    service.updateUser(...);
    service.deleteUser(...);
    service.validateEmail(...);
    // ... 100 more lines ...
    // When this fails, which part failed?
}

// ✅ CORRECT: One test per behavior
@Test
void shouldCreateUser() { /* ... */ }

@Test
void shouldUpdateUser() { /* ... */ }

@Test
void shouldDeleteUser() { /* ... */ }

@Test
void shouldValidateEmail() { /* ... */ }
```

**Impact:** Unclear failures, hard to debug, difficult to maintain.

### 7. Swallowing Exceptions

```java
// ❌ ANTI-PATTERN: Catching and ignoring exceptions
@Test
void badTest() {
    try {
        service.doSomethingThatShouldFail();
        // Test passes even though it should fail!
    } catch (Exception e) {
        // Silently ignored
    }
}

// ✅ CORRECT: Use assertThrows
@Test
void goodTest() {
    assertThrows(IllegalArgumentException.class, 
        () -> service.doSomethingThatShouldFail());
}
```

**Impact:** False positives, bugs slip through, loss of confidence in tests.

### 8. Environment-Specific Tests

```java
// ❌ ANTI-PATTERN: Tests depending on environment
@Test
void badTest() {
    String dbUrl = System.getenv("DATABASE_URL");
    assertNotNull(dbUrl);  // Fails on different machines!
    
    File config = new File("/etc/myapp/config.json");
    assertTrue(config.exists());  // Fails on Windows!
}

// ✅ CORRECT: Tests work everywhere
@Test
void goodTest() {
    var repository = mock(UserRepository.class);
    // No environment dependencies
}
```

**Impact:** Tests fail on other developer machines, can't run in CI, frustration.

## Summary: Quick Reference

### Test Execution Speed

| Practice | Speed Impact |
|----------|--------------|
| Mock external dependencies | 100-1000x faster |
| Enable parallel execution | 2-8x faster |
| Avoid Thread.sleep() | 10-100x faster |
| Use @BeforeEach for setup | Cleaner, no impact |
| Fresh mocks per test | Negligible, necessary |

### Test Isolation Checklist

- ✅ No static mutable fields
- ✅ Fresh mocks in @BeforeEach
- ✅ No shared collections between tests
- ✅ No test execution order dependencies
- ✅ Use ConcurrentHashMap for manual mocks
- ✅ Each test can run independently
- ✅ Enable parallel execution with @Execution(CONCURRENT)

### Service Design Checklist

- ✅ Use constructor injection
- ✅ Program to interfaces
- ✅ No hard-coded dependencies
- ✅ No new keyword for dependencies
- ✅ No static method calls to external services
- ✅ Keep business logic separate from infrastructure

### Mocking Decision Guide

**Choose Mockito when:**
- Complex interaction verification needed
- Multiple dependencies
- Team already uses it
- Need argument captors

**Choose hand-rolled mocks when:**
- Very simple interface (2-3 methods)
- Want to avoid dependencies
- Learning purposes
- Need to test state changes

## Conclusion

Effective unit testing is fundamental to software quality. By following these practices:

1. **Design for testability** - Use dependency injection and interfaces
2. **Ensure test isolation** - Fresh mocks, no shared state
3. **Enable parallel execution** - Dramatically faster test suites
4. **Choose appropriate mocking** - Mockito for complex, manual for simple
5. **Avoid anti-patterns** - No external dependencies, no shared state, no Thread.sleep()

Your test suite will be fast, reliable, and maintainable. Tests will run in milliseconds, execute in parallel, and give you confidence to refactor and deploy with ease.

## Complete Code Examples

All code examples from this blog post are available in the repository:
- **Testable service design**: [UserService.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/testing/unittesting/UserService.java)
- **Mockito tests**: [UserServiceTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/testing/unittesting/UserServiceTest.java)
- **Hand-rolled mocks**: [UserServiceManualMockTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/testing/unittesting/UserServiceManualMockTest.java)
- **Parallel execution**: [ParallelExecutionTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/testing/unittesting/ParallelExecutionTest.java)
- **Anti-patterns**: [BadUserService.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/testing/unittesting/antipatterns/BadUserService.java), [BadTestExamples.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/testing/unittesting/antipatterns/BadTestExamples.java)

## Further Reading

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Test-Driven Development by Example](https://www.oreilly.com/library/view/test-driven-development/0321146530/)
- [Growing Object-Oriented Software, Guided by Tests](https://www.oreilly.com/library/view/growing-object-oriented-software/9780321574442/)
