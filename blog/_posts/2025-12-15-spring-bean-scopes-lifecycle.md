---
layout: post
title: "Spring Bean Scopes and Lifecycle Management"
description: "How Spring creates, manages, and destroys beans — covering singleton, prototype, request, session, and application scopes, lifecycle callbacks (@PostConstruct, @PreDestroy), and the classic prototype-in-singleton trap."
date: 2025-12-15 14:00:00 +0000
categories: [interview]
tags: [java, java21, spring, spring-boot, beans, scopes, lifecycle, interview-preparation]
---

Imagine you are hiring staff for a restaurant. Some roles — the head chef — there is exactly one, and everyone talks to the same person. Other roles — waitstaff — you want a fresh one assigned per table. And some roles only exist while a customer is seated at their table; the moment they leave, that person is gone.

That is Spring bean scopes in a nutshell. Spring manages your objects (beans) for you, and *scope* determines how many instances exist and for how long. Get it wrong and you will spend a Friday afternoon debugging a shopping cart that happily mixes up everyone's orders.

Let's dig in.

## What Is a Spring Bean?

When Spring starts up, it reads your configuration (annotations, XML, or Java `@Configuration` classes), creates instances of your classes, wires them together, and stores them in a container called the **ApplicationContext**. Those managed instances are *beans*.

The *scope* of a bean tells Spring:
1. How many instances to create
2. When to create them
3. When to destroy them

## The Five Standard Scopes

<div class="table-wrapper" markdown="1">

| Scope | Instances | Lifecycle | Use When |
|---|---|---|---|
| `singleton` | One per ApplicationContext | Lives as long as the context | Stateless services, repositories |
| `prototype` | New one per request | Not managed after creation | Stateful helpers, commands |
| `request` | One per HTTP request | Destroyed when request ends | Web: per-request state |
| `session` | One per HTTP session | Destroyed when session expires | Web: user session state (e.g. shopping cart) |
| `application` | One per ServletContext | Lives as long as the web app | Web: app-wide shared state |

</div>

The last three (`request`, `session`, `application`) are only available in a Spring web application context.

## Singleton — The Default (and the One Everyone Forgets Is the Default)

Spring's default scope is **singleton**. One instance per `ApplicationContext`, cached, reused, handed out to everyone who asks.

```java
@Service  // @Scope("singleton") is the default — you don't need to say it
public class OrderService {

    // This counter is shared across ALL callers — all requests, all threads
    private int ordersProcessed = 0;

    public OrderConfirmation placeOrder(Order order) {
        ordersProcessed++;  // ⚠️ Race condition waiting to happen
        // ...
        return new OrderConfirmation(order.id());
    }
}
```

**The golden rule:** singleton beans must be **stateless** (or use thread-safe state). Because there's only one instance and it handles all concurrent requests, any mutable field is a race condition. Use `final` fields, local variables, and thread-local storage when you need per-request context.

If you're coming from Scala, think of a singleton bean like a `given` instance at the top of your implicit scope — one value for the whole program.

## Prototype — A Fresh Bean Every Time

Prototype scope creates a **new instance every time** the bean is requested from the container. Spring creates it, injects dependencies into it, and then *hands it to you and forgets about it*. No caching. No `@PreDestroy` lifecycle callback. You own it now.

```java
@Component
@Scope("prototype")
public class ReportBuilder {

    private final List<String> sections = new ArrayList<>();  // safe — new list per instance

    public void addSection(String content) {
        sections.add(content);
    }

    public String build() {
        return String.join("\n\n", sections);
    }
}
```

Every time Spring creates a `ReportBuilder`, you get a clean slate. Perfect for stateful objects that should not bleed state between callers.

## The Classic Trap: Prototype Inside a Singleton

This one trips up almost everyone eventually.

```java
@Service  // singleton — one instance
public class ReportService {

    @Autowired
    private ReportBuilder builder;  // ⚠️ prototype injected into singleton!

    public String generateReport(List<String> sections) {
        // builder is the SAME instance every time — it accumulates sections across calls!
        sections.forEach(builder::addSection);
        return builder.build();
    }
}
```

Spring injects the `ReportBuilder` *once*, when `ReportService` is created. Even though `ReportBuilder` is prototype-scoped, you still get the same (now stale) instance on every call. The singleton's startup wiring happened once — that's it.

**The fix:** use `ObjectProvider<T>`, which is Spring's factory interface:

```java
@Service  // singleton
public class ReportService {

    private final ObjectProvider<ReportBuilder> builderFactory;

    // ObjectProvider<T> is injected as a factory, not as an instance
    public ReportService(ObjectProvider<ReportBuilder> builderFactory) {
        this.builderFactory = builderFactory;
    }

    public String generateReport(List<String> sections) {
        // getObject() creates a fresh ReportBuilder every time
        var builder = builderFactory.getObject();
        sections.forEach(builder::addSection);
        return builder.build();
    }
}
```

Now `getObject()` asks the container for a new prototype each time. The singleton acts as a factory without holding a stale reference.

Alternative approaches: `@Lookup`-annotated methods, `javax.inject.Provider<T>`, or `ApplicationContext.getBean()` (though that last one couples your code to the container and should be a last resort).

## Lifecycle Callbacks: @PostConstruct and @PreDestroy

Every Spring bean goes through a predictable lifecycle:

```
Instantiation → Dependency injection → @PostConstruct → [in use] → @PreDestroy → Destruction
```

You can hook into the two most useful phases with annotations:

```java
@Service
public class ConnectionPool {

    private HikariDataSource dataSource;

    @PostConstruct  // Spring calls this after all @Autowired fields are injected
    public void initialise() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        dataSource.setMaximumPoolSize(10);
        System.out.println("Pool open — ready to take connections!");
    }

    @PreDestroy  // Spring calls this before removing the bean from the context
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Pool closed — see you after the next restart.");
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
```

`@PostConstruct` is the right place to set up resources that depend on injected collaborators (you cannot use the constructor because Spring hasn't injected anything yet at that point). `@PreDestroy` is the right place to release those resources cleanly when the application shuts down.

Both annotations come from `jakarta.annotation-api` (formerly `javax.annotation`) — they are *not* Spring-specific, which makes them portable and testable without a Spring context.

> **Note:** `@PreDestroy` is only called for **singleton** beans. Prototype beans are handed off and forgotten — you are responsible for cleanup. This is a common gotcha.

## The InitializingBean / DisposableBean Interfaces

Before annotations became fashionable, Spring provided interfaces for the same purpose:

```java
@Service
public class CacheManager implements InitializingBean, DisposableBean {

    private Map<String, Object> cache;

    @Override
    public void afterPropertiesSet() {  // equivalent of @PostConstruct
        cache = new ConcurrentHashMap<>();
        System.out.println("Cache initialised — warming up...");
    }

    @Override
    public void destroy() {  // equivalent of @PreDestroy
        cache.clear();
        System.out.println("Cache cleared — memory returned.");
    }
}
```

These interfaces work perfectly well, but they couple your class to Spring's API. The annotation approach (`@PostConstruct` / `@PreDestroy`) keeps your code Spring-agnostic and is the idiomatic modern choice.

## @Bean with initMethod / destroyMethod

For beans you define with `@Bean` (typically third-party classes you cannot annotate), you can specify lifecycle methods directly on the annotation:

```java
@Configuration
public class InfrastructureConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedKafka kafka() {
        return new EmbeddedKafka("localhost", 9092);
    }
}
```

Spring will call `start()` after creating the bean and `stop()` before destroying it, without you having to change the `EmbeddedKafka` class at all. Very handy for library classes.

## The Complete Lifecycle, Visualised

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring ApplicationContext                      │
│                                                                   │
│  1. Instantiate bean (calls constructor)                          │
│  2. Inject dependencies (@Autowired, constructor, setter)         │
│  3. Call BeanPostProcessor.beforeInitialization()                 │
│  4. Call @PostConstruct / afterPropertiesSet() / initMethod       │
│  5. Call BeanPostProcessor.afterInitialization()                  │
│  6. Bean is ready — serve requests                                │
│  7. [Context shutting down]                                       │
│  8. Call @PreDestroy / destroy() / destroyMethod  (singleton only)│
│  9. Bean is gone                                                  │
└─────────────────────────────────────────────────────────────────┘
```

Most day-to-day Spring development only touches steps 1–6 and 8. The `BeanPostProcessor` steps (3 and 5) are the extension point that makes things like `@Transactional` and `@Cacheable` work — Spring wraps your bean in a proxy at those steps.

## Web Scopes: Request, Session, Application

In a Spring MVC application, three additional scopes become available:

```java
// New instance per HTTP request
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    private final String requestId = UUID.randomUUID().toString();
    private String currentUserId;
    // ...
}

// One instance per user session (survives across multiple requests)
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    // ...
}
```

The `proxyMode = ScopedProxyMode.TARGET_CLASS` part is important: because `ShoppingCart` lives shorter than the singleton beans that depend on it, Spring creates a *proxy* in their place. The proxy knows how to look up the real `ShoppingCart` for the current session at runtime — similar to the prototype-in-singleton problem, but solved automatically by Spring's proxy machinery.

## Quick Reference: Scope Comparison

<div class="table-wrapper" markdown="1">

| Question | Answer |
|---|---|
| What is the default scope? | `singleton` |
| Which scope is best for stateless services? | `singleton` |
| Which scope gives you a new instance every time? | `prototype` |
| Does Spring call `@PreDestroy` on prototype beans? | No — you own them after creation |
| How do you inject a prototype into a singleton correctly? | Use `ObjectProvider<T>` or `@Lookup` |
| Which annotation replaces `InitializingBean`? | `@PostConstruct` |
| Which annotation replaces `DisposableBean`? | `@PreDestroy` |
| What `proxyMode` is needed for web-scoped beans in singletons? | `ScopedProxyMode.TARGET_CLASS` |

</div>

## When to Use Which Scope

- **`singleton`** — almost everything: services, repositories, controllers, configuration. Stateless and safe by construction.
- **`prototype`** — stateful objects that should not be shared: command objects, builders, per-operation state. Remember: you are responsible for cleanup.
- **`request`** — per-HTTP-request context: request IDs, audit trails, user identity extracted from a JWT.
- **`session`** — per-user session state: shopping carts, wizard flow state, preferences. Keep these small — they live in memory (or a session store) for the duration of the session.
- **`application`** — app-wide shared state that is not a singleton for some reason (rare).

## Conclusion

Spring's scoping system is a lot simpler than it first appears: the default (singleton) is correct for stateless objects, prototype gives you a fresh instance per use, and the web scopes (request/session/application) mirror the natural lifecycles of a web request.

The lifecycle hooks (`@PostConstruct` and `@PreDestroy`) let you plug resource management cleanly into Spring's startup and shutdown sequence — no overriding framework base classes, no static initialisers, just annotated methods. This is the idiomatic, testable approach.

If you're coming from Scala, you'll find the mental model maps well: singletons behave like top-level `given` instances, prototypes like calling a factory function, and the lifecycle callbacks like `Resource.make` — acquire on open, release on close.

## Code Samples

All examples in this post are illustrated with runnable plain-Java code in the repository
(no Spring runtime required — the same patterns, tested with JUnit 5):

`java21/src/main/java/io/github/sps23/spring/scopes/`

- [`ConnectionPool.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/ConnectionPool.java) — lifecycle init/destroy pattern (`@PostConstruct` / `@PreDestroy` equivalent)
- [`Counter.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/Counter.java) — simple stateful bean illustrating singleton vs prototype behaviour
- [`BeanScopeSimulator.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/BeanScopeSimulator.java) — simulates singleton caching vs prototype factory using a `Supplier<T>`

`java21/src/test/java/io/github/sps23/spring/scopes/`

- [`ConnectionPoolTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/scopes/ConnectionPoolTest.java) — verifies the full lifecycle: open → use → close
- [`BeanScopeSimulatorTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/scopes/BeanScopeSimulatorTest.java) — proves singleton shares state while prototype does not; demonstrates the factory fix for prototype-in-singleton

Run the tests yourself with:

```bash
./gradlew :java21:test --tests "io.github.sps23.spring.scopes.*"
```

---

*This post is part of the [Spring Framework Interview Preparation series]({{ site.baseurl }}{% link _posts/2025-12-14-spring-framework-interview-preparation-guide.md %}). Check out the full plan for all Spring topics.*
