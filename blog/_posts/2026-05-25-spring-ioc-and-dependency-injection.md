---
layout: post
title: "Inversion of Control and Dependency Injection in Spring"
description: "How Spring's IoC container manages your beans so you don't have to - covering constructor vs setter injection, @Autowired, @Component, @Configuration, and making your code actually testable."
date: 2026-05-25 15:00:00 +0000
categories: [interview]
tags: [java, java21, spring, spring-boot, dependency-injection, ioc, interview-preparation]
---

Picture this: you are a Java developer in 2003. Every class you write creates its own dependencies. Your `OrderService` creates a new `PaymentGateway`, which creates a new `DatabaseConnection`, which creates a new `ConnectionPool`. You change one class and the entire house of cards wobbles. Tests are a fairy tale because you cannot swap out the real payment gateway for a fake one — it is hardwired in.

Then Spring came along and said: *"Relax. We'll manage that for you."*

This is **Inversion of Control**: instead of your code creating and wiring objects together, you hand that responsibility to a framework container. The objects (beans, in Spring-speak) declare what they need, and Spring figures out how to assemble the whole graph. No `new`, no static singletons, no elaborate factory patterns.

If you're coming from Scala, this will feel oddly familiar — it's like having an implicit/`given` system for your entire object graph. Except it's runtime, annotation-driven, and occasionally magical in ways that will make you squint at stack traces.

Let's dig in.

## The Problem: Tight Coupling Kills Testability

Here's the villain of our story:

```java
// ❌ The "Just New Everything" anti-pattern
public class OrderService {

    // Hard-wired — you cannot swap this out in tests
    private final PaymentGateway paymentGateway = new StripePaymentGateway();
    private final EmailSender emailSender = new SmtpEmailSender("smtp.company.com");
    private final OrderRepository repository = new PostgresOrderRepository();

    public OrderConfirmation placeOrder(Order order) {
        var payment = paymentGateway.charge(order.amount(), order.customerId());
        repository.save(order);
        emailSender.send(order.customerEmail(), "Your order is confirmed!");
        return new OrderConfirmation(order.id(), payment.transactionId());
    }
}
```

**What's wrong here?**

1. Every test that uses `OrderService` hits a real Stripe API, a real database, and sends real emails. Your CI bill will be enormous. Your test suite will be slow and brittle.
2. Want to swap Stripe for PayPal? You have to change `OrderService`. That violates the Open/Closed Principle and is the kind of thing that causes production incidents at 2 AM.
3. The class is lying about its dependencies. To understand what it needs, you have to read its implementation, not its constructor signature.

The fix is simple: **program to interfaces and receive dependencies from outside**.

```java
// ✅ Interfaces make dependencies swappable
public interface PaymentGateway {
    PaymentResult charge(BigDecimal amount, String customerId);
}

public interface EmailSender {
    void send(String to, String subject);
}

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
}
```

Now `OrderService` can declare what it needs without caring about the implementation:

```java
// ✅ Dependencies declared, not created
public class OrderService {

    private final PaymentGateway paymentGateway;
    private final EmailSender emailSender;
    private final OrderRepository repository;

    public OrderService(
            PaymentGateway paymentGateway,
            EmailSender emailSender,
            OrderRepository repository) {
        this.paymentGateway = paymentGateway;
        this.emailSender = emailSender;
        this.repository = repository;
    }

    public OrderConfirmation placeOrder(Order order) {
        var payment = paymentGateway.charge(order.amount(), order.customerId());
        repository.save(order);
        emailSender.send(order.customerEmail(), "Your order is confirmed!");
        return new OrderConfirmation(order.id(), payment.transactionId());
    }
}
```

Spring will handle the "who creates what and passes it to whom" part. That's Dependency Injection.

## Spring's IoC Container

The **IoC container** is the beating heart of Spring. It reads your configuration (annotations, Java `@Configuration` classes, or the ancient XML scrolls), figures out the full dependency graph, creates everything in the right order, and hands you a fully assembled application.

The primary interface is `ApplicationContext`. You rarely interact with it directly in Spring Boot (it's hidden behind the `@SpringBootApplication` magic), but knowing it exists explains a lot:

```java
// This is what Spring Boot is doing under the hood
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

// Retrieve a fully-assembled bean — all its dependencies already injected
OrderService orderService = context.getBean(OrderService.class);
```

The container maintains a registry of **beans** — objects it manages. When you ask for a bean, Spring looks up the registry, creates instances if needed, injects their dependencies, runs lifecycle callbacks, and returns the ready-to-use object.

## Registering Beans: Three Ways to Do It

Spring gives you three ways to tell it about your beans. You'll see all three in the wild.

### Method 1: Component Scanning (Most Common)

Annotate your classes with stereotype annotations and Spring finds them automatically:

```java
// @Component is the generic "I am a Spring bean" annotation
@Component
public class StripePaymentGateway implements PaymentGateway {

    private final String apiKey;

    // Spring injects this from your configuration properties
    public StripePaymentGateway(@Value("${stripe.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public PaymentResult charge(BigDecimal amount, String customerId) {
        // Call the Stripe API...
        return new PaymentResult("txn_" + customerId, amount);
    }
}

// Specialized stereotype annotations (same as @Component but more descriptive)
@Repository  // Data access layer — also enables exception translation
public class JpaOrderRepository implements OrderRepository {
    // ...
}

@Service     // Business logic layer
public class OrderService {
    // Spring will inject the beans above automatically
    public OrderService(
            PaymentGateway paymentGateway,
            EmailSender emailSender,
            OrderRepository repository) {
        // ...
    }
}

@Controller  // Web layer (handles HTTP requests)
@RestController  // = @Controller + @ResponseBody (returns JSON/XML directly)
public class OrderController {
    // ...
}
```

Spring scans packages for these annotations when it starts up. In Spring Boot, `@SpringBootApplication` triggers scanning of the package it's in and all sub-packages.

### Method 2: Java Configuration (Explicit Control)

When you need to configure beans programmatically — third-party libraries, conditional logic, complex setup — use `@Configuration`:

```java
@Configuration
public class AppConfig {

    // Each @Bean method produces a bean managed by Spring
    @Bean
    public PaymentGateway paymentGateway(
            @Value("${stripe.api-key}") String apiKey,
            @Value("${stripe.timeout-ms:5000}") int timeoutMs) {
        var gateway = new StripePaymentGateway(apiKey);
        gateway.setTimeoutMs(timeoutMs);
        return gateway;
    }

    @Bean
    public EmailSender emailSender(
            @Value("${smtp.host}") String host,
            @Value("${smtp.port:587}") int port) {
        return new SmtpEmailSender(host, port);
    }

    // Spring automatically injects the paymentGateway and emailSender beans above
    @Bean
    public OrderService orderService(
            PaymentGateway paymentGateway,
            EmailSender emailSender,
            OrderRepository repository) {
        return new OrderService(paymentGateway, emailSender, repository);
    }
}
```

The beauty of `@Configuration`: the method parameters are injected by Spring, so the `orderService` bean automatically receives the beans defined in the same class (or found elsewhere in the context).

### Method 3: XML Configuration (Please Don't)

XML configuration still works and you will find it in legacy codebases. It looks like this, and when you see it, you have my permission to shudder slightly:

```xml
<!-- applicationContext.xml — written by developers who enjoyed suffering -->
<beans>
    <bean id="paymentGateway" class="com.example.StripePaymentGateway">
        <constructor-arg name="apiKey" value="${stripe.api-key}"/>
    </bean>
    <bean id="orderService" class="com.example.OrderService">
        <constructor-arg ref="paymentGateway"/>
        <constructor-arg ref="emailSender"/>
        <constructor-arg ref="orderRepository"/>
    </bean>
</beans>
```

It works. It's also verbose, not type-safe, and refactoring it is a special kind of pain. Stick with annotations or Java config for new code.

## Constructor Injection vs. Setter Injection

Spring can inject dependencies in two main ways. One is correct. One is a trap.

### Constructor Injection (The Right Way™)

```java
@Service
public class OrderService {

    private final PaymentGateway paymentGateway;
    private final EmailSender emailSender;
    private final OrderRepository repository;

    // Spring calls this constructor and provides all arguments
    // In Spring 4.3+, @Autowired is optional when there's a single constructor
    public OrderService(
            PaymentGateway paymentGateway,
            EmailSender emailSender,
            OrderRepository repository) {
        this.paymentGateway = paymentGateway;
        this.emailSender = emailSender;
        this.repository = repository;
    }
}
```

**Why constructor injection wins:**

1. **Immutability**: Fields can be `final`. The object is fully initialized the moment it's created. No partially-constructed zombie objects.
2. **Mandatory dependencies**: If Spring cannot satisfy a constructor argument, it fails at startup — not at 3 AM when someone calls the endpoint that exercises that code path.
3. **Testability without Spring**: You can create an `OrderService` in a test with `new OrderService(mockGateway, mockEmail, mockRepo)`. No Spring context needed. Fast tests.
4. **No circular dependencies at runtime**: Spring detects constructor-injection cycles at startup and throws a descriptive error instead of an infinite loop.

### Setter Injection (Use Sparingly)

```java
@Service
public class OrderService {

    private PaymentGateway paymentGateway;
    private EmailSender emailSender;

    // Spring calls these setters after construction
    @Autowired
    public void setPaymentGateway(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Autowired
    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }
}
```

**When setter injection is acceptable:**
- Optional dependencies (where a reasonable default exists if not injected)
- Circular dependencies that genuinely cannot be restructured (a code smell, but sometimes inherited)
- Legacy code where you cannot change the constructor

**The dark side:** setter-injected fields cannot be `final`. Your object can be used before all dependencies are set. Tests need a Spring context or manual setter calls to work. It's just... messier.

### Field Injection (The "No" Lane)

```java
@Service
public class OrderService {

    @Autowired  // ❌ Don't do this in production code
    private PaymentGateway paymentGateway;

    @Autowired
    private EmailSender emailSender;
}
```

Field injection looks clean but is secretly terrible:
- Cannot be `final`
- Impossible to instantiate the class in tests without a Spring context (or reflection hacks)
- Hidden dependencies — you have to read the field declarations to know what the class needs
- IntelliJ will give you a warning about this. IntelliJ is right.

The only legitimate use case for field injection is in tests themselves, where `@InjectMocks` or `@Autowired` in `@SpringBootTest` is more convenient than constructors.

## @Autowired: Spring's Matchmaking Service

When Spring sees `@Autowired` on a constructor, setter, or method parameter, it searches the ApplicationContext for a bean of the matching type and injects it.

```java
@Service
public class NotificationService {

    private final EmailSender emailSender;
    private final SmsSender smsSender;

    public NotificationService(EmailSender emailSender, SmsSender smsSender) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }
}
```

**What if there are multiple beans of the same type?** Spring throws `NoUniqueBeanDefinitionException`. You fix this with `@Qualifier`:

```java
@Configuration
public class EmailConfig {

    @Bean("transactionalEmail")
    public EmailSender transactionalEmailSender() {
        return new SmtpEmailSender("smtp.company.com");
    }

    @Bean("marketingEmail")
    public EmailSender marketingEmailSender() {
        return new SendgridEmailSender("api-key-here");
    }
}

@Service
public class OrderService {

    private final EmailSender emailSender;

    public OrderService(@Qualifier("transactionalEmail") EmailSender emailSender) {
        this.emailSender = emailSender;
    }
}
```

**Injecting a List of all implementations** — a neat trick for plugin-style architectures:

```java
@Service
public class AuditService {

    // Spring injects ALL beans implementing AuditHandler — in order if @Order is used
    private final List<AuditHandler> handlers;

    public AuditService(List<AuditHandler> handlers) {
        this.handlers = handlers;
    }

    public void audit(AuditEvent event) {
        handlers.forEach(h -> h.handle(event));
    }
}

// Any class annotated @Component that implements AuditHandler is automatically included
@Component
@Order(1)
public class LoggingAuditHandler implements AuditHandler { /* ... */ }

@Component
@Order(2)
public class DatabaseAuditHandler implements AuditHandler { /* ... */ }

@Component
@Order(3)
public class MetricsAuditHandler implements AuditHandler { /* ... */ }
```

**Injecting a Map** of bean-name to implementation:

```java
@Service
public class PaymentRouter {

    // Map key = bean name, value = implementation
    private final Map<String, PaymentGateway> gateways;

    public PaymentRouter(Map<String, PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentResult route(String provider, BigDecimal amount, String customerId) {
        var gateway = gateways.get(provider + "PaymentGateway");
        if (gateway == null) {
            throw new IllegalArgumentException("Unknown payment provider: " + provider);
        }
        return gateway.charge(amount, customerId);
    }
}
```

## The Bean Lifecycle (Cliff Notes)

Spring beans go through a well-defined lifecycle. You can hook into it:

```java
@Service
public class DatabaseConnectionPool {

    private HikariDataSource dataSource;

    @PostConstruct  // Called after Spring injects all dependencies
    public void init() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        dataSource.setMaximumPoolSize(10);
        System.out.println("Connection pool ready — let the queries flow!");
    }

    @PreDestroy  // Called when the application context is shutting down
    public void cleanup() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Connection pool closed — see you next restart.");
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
```

`@PostConstruct` and `@PreDestroy` are the idiomatic modern approach. They're from `jakarta.annotation` (previously `javax.annotation`) and work without any Spring-specific interfaces.

## Making Your Code Testable

This is where all the wiring pays off. With constructor injection and interfaces, your unit tests become a pleasure:

```java
// ✅ Fast, isolated unit test — no Spring context, no database, no real email
class OrderServiceTest {

    private PaymentGateway mockGateway;
    private EmailSender mockEmailSender;
    private OrderRepository mockRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        mockGateway = Mockito.mock(PaymentGateway.class);
        mockEmailSender = Mockito.mock(EmailSender.class);
        mockRepository = Mockito.mock(OrderRepository.class);
        orderService = new OrderService(mockGateway, mockEmailSender, mockRepository);
    }

    @Test
    void shouldChargePaymentAndSendConfirmationEmail() {
        // Arrange
        var order = new Order("order-1", "cust-42", new BigDecimal("99.99"), "test@example.com");
        var paymentResult = new PaymentResult("txn_abc123", new BigDecimal("99.99"));
        Mockito.when(mockGateway.charge(order.amount(), order.customerId()))
               .thenReturn(paymentResult);

        // Act
        var confirmation = orderService.placeOrder(order);

        // Assert
        assertThat(confirmation.orderId()).isEqualTo("order-1");
        assertThat(confirmation.transactionId()).isEqualTo("txn_abc123");
        Mockito.verify(mockRepository).save(order);
        Mockito.verify(mockEmailSender).send("test@example.com", "Your order is confirmed!");
    }

    @Test
    void shouldPropagatePaymentFailure() {
        var order = new Order("order-2", "cust-99", new BigDecimal("500.00"), "test@example.com");
        Mockito.when(mockGateway.charge(any(), any()))
               .thenThrow(new PaymentException("Card declined"));

        assertThatThrownBy(() -> orderService.placeOrder(order))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Card declined");

        // Verify we didn't save a failed order or send a false confirmation
        Mockito.verify(mockRepository, never()).save(any());
        Mockito.verify(mockEmailSender, never()).send(any(), any());
    }
}
```

No Spring. No database. No email server. Just pure, fast, reliable unit tests that run in milliseconds.

For integration tests where you *do* want Spring to wire things up, use `@SpringBootTest`:

```java
@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;  // fully assembled by Spring

    @MockBean  // Replaces the real bean in the context with a Mockito mock
    private PaymentGateway paymentGateway;

    @Test
    void shouldProcessOrderEndToEnd() {
        Mockito.when(paymentGateway.charge(any(), any()))
               .thenReturn(new PaymentResult("txn_integration", new BigDecimal("25.00")));

        var order = new Order("ord-42", "cust-1", new BigDecimal("25.00"), "user@test.com");
        var confirmation = orderService.placeOrder(order);

        assertThat(confirmation.transactionId()).isEqualTo("txn_integration");
    }
}
```

## Key Concepts at a Glance

<div class="table-wrapper" markdown="1">

| Concept | What It Does | When to Use |
|---|---|---|
| `@Component` | Marks a class as a Spring-managed bean | Generic utility classes |
| `@Service` | Specialized `@Component` for business logic | Service layer |
| `@Repository` | Specialized `@Component` for data access | DAO/repository classes |
| `@Controller` / `@RestController` | Specialized `@Component` for web layer | HTTP handlers |
| `@Configuration` | Marks a class as a source of `@Bean` methods | Third-party libs, complex wiring |
| `@Bean` | Produces a bean from a method in a `@Configuration` class | Manual bean creation |
| `@Autowired` | Requests dependency injection | Constructor/setter/field injection |
| `@Qualifier` | Disambiguates when multiple beans of same type exist | Multiple implementations |
| `@Value` | Injects a property value | Configuration values |
| `@PostConstruct` | Runs after all dependencies are injected | Initialisation logic |
| `@PreDestroy` | Runs before the bean is destroyed | Cleanup / resource release |

</div>

## Constructor vs Setter vs Field Injection — The Verdict

<div class="table-wrapper" markdown="1">

| Criterion | Constructor | Setter | Field |
|---|---|---|---|
| Dependencies can be `final` | ✅ Yes | ❌ No | ❌ No |
| Object always fully initialized | ✅ Yes | ❌ No | ❌ No |
| Mandatory dependencies enforced at startup | ✅ Yes | ❌ No | ❌ No |
| Testable without Spring context | ✅ Yes (just `new`) | ⚠️ Needs setters | ❌ Needs reflection |
| Circular dependency detection | ✅ At startup | ⚠️ At runtime | ⚠️ At runtime |
| Optional dependencies supported | ⚠️ Needs `@Nullable` | ✅ Natural fit | ✅ Natural fit |
| Recommended for | Mandatory deps | Optional deps | Test-only fields |

</div>

Spring's own documentation, Google's Java style guide, and basically every experienced Spring developer will tell you the same thing: **use constructor injection for mandatory dependencies**. The compiler and the container both help you when you do.

## Conclusion

Spring's IoC container flips the dependency-creation responsibility from your classes to the framework. You declare what your classes need; Spring figures out the assembly order and wires everything together.

For Scala developers, the mental model is simpler than it might first appear: think of `@Component`/`@Service` as declaring something like `given MyService: ServiceImpl = ServiceImpl(dep1, dep2)`, with Spring doing the implicit resolution at startup time — but with annotation syntax instead of Scala's type-class machinery, and with runtime reflection instead of compile-time inference.

The key takeaway: constructor injection + interfaces + letting Spring manage the wiring = code that is modular, testable, and easy to change. That's not magic. It's just good engineering that Spring makes the path of least resistance.

## Code Samples

All examples in this post are runnable. Find them in the repository under
`java21/src/main/java/io/github/sps23/spring/ioc/`:

**Interfaces and domain model**
- [`PaymentGateway.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/PaymentGateway.java) — payment provider abstraction
- [`EmailSender.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/EmailSender.java) — email delivery abstraction
- [`OrderRepository.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/OrderRepository.java) — order persistence abstraction
- [`AuditHandler.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/AuditHandler.java) — plugin interface for audit events
- [`Order.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/Order.java), [`OrderConfirmation.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/OrderConfirmation.java), [`PaymentResult.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/PaymentResult.java), [`AuditEvent.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/AuditEvent.java) — domain records

**Services (demonstrating injection patterns)**
- [`OrderService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/OrderService.java) — constructor injection of mandatory dependencies
- [`AuditService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/AuditService.java) — `List<AuditHandler>` injection for plugin-style dispatch
- [`PaymentRouter.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/PaymentRouter.java) — `Map<String, PaymentGateway>` injection for provider routing

**Stub/fake implementations (for testing without Spring)**
- [`FakePaymentGateway.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/FakePaymentGateway.java) — configurable fake that can simulate failures
- [`StubEmailSender.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/StubEmailSender.java) — records sent emails for assertion
- [`InMemoryOrderRepository.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/InMemoryOrderRepository.java) — thread-safe in-memory store
- [`LoggingAuditHandler.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/LoggingAuditHandler.java), [`MetricsAuditHandler.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/MetricsAuditHandler.java), [`DatabaseAuditHandler.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/ioc/DatabaseAuditHandler.java) — audit handler implementations

**Tests (no Spring context required)**
- [`OrderServiceTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/ioc/OrderServiceTest.java) — Mockito mocks + hand-rolled stubs; proves constructor injection = no-Spring testability
- [`AuditServiceTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/ioc/AuditServiceTest.java) — verifies `List<AuditHandler>` dispatch, ordering, and edge cases
- [`PaymentRouterTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/ioc/PaymentRouterTest.java) — verifies `Map<String, PaymentGateway>` routing and failure propagation

Run the tests yourself with:

```bash
./gradlew :java21:test --tests "io.github.sps23.spring.ioc.*"
```

---

*This post is part of the [Spring Framework Interview Preparation series]({{ site.baseurl }}{% link _posts/2025-12-14-spring-framework-interview-preparation-guide.md %}). Check out the full plan for all Spring topics.*
