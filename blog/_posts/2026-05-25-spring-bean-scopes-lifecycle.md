---
layout: post
title: "Spring Bean Scopes and Lifecycle Management"
description: "How Spring creates, manages, and destroys beans — covering singleton, prototype, request, session, and application scopes, lifecycle callbacks (@PostConstruct, @PreDestroy), and the classic prototype-in-singleton trap."
date: 2026-05-25 20:00:00 +0000
updated: 2026-07-26 11:25:00 +0000
categories: [interview]
tags: [java, java21, spring, bean-scopes, lifecycle, interview-preparation]
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
public class OrderVolumeTracker {

    // This counter is shared across ALL callers — all requests, all threads
    private int ordersProcessed;

    public synchronized int recordOrderProcessed() {
        ordersProcessed++;  // ⚠️ Race condition waiting to happen without synchronized
        return ordersProcessed;
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
public class StaleReportService {

    private final ReportBuilder builder;  // ⚠️ prototype resolved once, at startup!

    public StaleReportService(ReportBuilder builder) {
        this.builder = builder;
    }

    public String generateReport(List<String> sections) {
        // builder is the SAME instance every time — it accumulates sections across calls!
        sections.forEach(builder::addSection);
        return builder.build();
    }
}
```

Spring injects the `ReportBuilder` *once*, when `StaleReportService` is created (this is real Spring behavior: constructor arguments are resolved a single time, at the singleton's creation). Even though `ReportBuilder` is prototype-scoped, you still get the same (now stale) instance on every call. The singleton's startup wiring happened once — that's it.

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

Alternative approaches: `@Lookup`-annotated methods, `jakarta.inject.Provider<T>`, or `ApplicationContext.getBean()` (though that last one couples your code to the container and should be a last resort). All three (`ObjectProvider`, `@Lookup`, and the stale-injection bug) are exercised by real Spring tests in this repository — see [Code Samples](#code-samples).

## Lifecycle Callbacks: @PostConstruct and @PreDestroy

Every Spring bean goes through a predictable lifecycle:

```
Instantiation → Dependency injection → @PostConstruct → [in use] → @PreDestroy → Destruction
```

You can hook into the two most useful phases with annotations:

```java
@Component
public class ManagedConnectionPool {

    private final Deque<String> connections = new ArrayDeque<>();
    private boolean open;

    @PostConstruct  // Spring calls this after all dependencies are injected
    void openPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            connections.push("connection-" + i);
        }
        open = true;
    }

    @PreDestroy  // Spring calls this before removing the bean from the context
    void closePool() {
        connections.clear();
        open = false;
    }

    public String borrowConnection() {
        // ...
    }
}
```

`@PostConstruct` is the right place to set up resources that depend on injected collaborators (you cannot use the constructor because Spring hasn't injected anything yet at that point). `@PreDestroy` is the right place to release those resources cleanly when the application shuts down.

Both annotations come from `jakarta.annotation-api` (formerly `javax.annotation`) — they are *not* Spring-specific, which makes them portable and testable without a Spring context.

> **Note:** `@PreDestroy` is only called for **singleton** beans. Prototype beans are handed off and forgotten — you are responsible for cleanup. This is a common gotcha.

## The InitializingBean / DisposableBean Interfaces

Before annotations became fashionable, Spring provided interfaces for the same purpose:

```java
@Component
public class LegacyCacheManager implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() {  // equivalent of @PostConstruct
        // warm up the cache...
    }

    @Override
    public void destroy() {  // equivalent of @PreDestroy
        // flush the cache...
    }
}
```

These interfaces work perfectly well, but they couple your class to Spring's API. The annotation approach (`@PostConstruct` / `@PreDestroy`) keeps your code Spring-agnostic and is the idiomatic modern choice.

## @Bean with initMethod / destroyMethod

For beans you define with `@Bean` (typically third-party classes you cannot annotate), you can specify lifecycle methods directly on the annotation:

```java
@Configuration
public class ScopesLifecycleConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedMessageBroker embeddedMessageBroker() {
        return new EmbeddedMessageBroker();  // a plain POJO you don't own/can't annotate
    }
}
```

Spring will call `start()` after creating the bean and `stop()` before destroying it, without you having to change the `EmbeddedMessageBroker` class at all. Very handy for library classes.

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
public class RequestTrace {
    private final String traceId = UUID.randomUUID().toString();
    // ...
}

// One instance per user session (survives across multiple requests)
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {
    private final List<String> items = new ArrayList<>();
    // ...
}
```

The `proxyMode = ScopedProxyMode.TARGET_CLASS` part is important: because `ShoppingCart` lives shorter than the singleton beans that depend on it, Spring creates a *proxy* in their place. The proxy knows how to look up the real `ShoppingCart` for the current session at runtime — similar to the prototype-in-singleton problem, but solved automatically by Spring's proxy machinery. This requires `spring-web` on the classpath (`WebApplicationContext`, the request/session scope implementations, and the `@RequestScope`/`@SessionScope` convenience annotations all live there).

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

## Spring 7 vs Spring 6: What Changed for Scopes and Lifecycle?

Good news if you're maintaining older code: **nothing** in the scope/lifecycle model itself changed between Spring Framework 6 and 7. `singleton`/`prototype`/`request`/`session`/`application` scopes, `@PostConstruct`/`@PreDestroy`, `InitializingBean`/`DisposableBean`, `ObjectProvider<T>`, `@Lookup`, and `@Bean(initMethod/destroyMethod)` all behave exactly as described above in both versions — this is one of the most stable corners of the framework.

What *did* change in Spring 7, in areas adjacent to this topic:

<div class="table-wrapper" markdown="1">

| Area | Spring 6 | Spring 7 |
|---|---|---|
| Bean scopes / lifecycle callbacks | Same API and behavior | Unchanged |
| Baseline Java version | Java 17 | Java 17 (no change) |
| Null-safety annotations | `org.springframework.lang.@Nullable`/`@NonNull` | Deprecated in favor of [JSpecify](https://jspecify.dev) `@Nullable` on the type usage, e.g. `private @Nullable String field` |
| Programmatic bean registration | `@Bean` methods / `BeanDefinitionRegistryPostProcessor` | New `BeanRegistrar` interface for first-class programmatic registration (see the [Spring Configuration post]({{ site.baseurl }}{% link _posts/2026-07-26-spring-configuration-approaches.md %})) |

</div>

In short: if you already know how singleton/prototype/web scopes and lifecycle callbacks work in Spring 6, that knowledge transfers to Spring 7 unchanged. Nothing to relearn here.

## Conclusion

Spring's scoping system is a lot simpler than it first appears: the default (singleton) is correct for stateless objects, prototype gives you a fresh instance per use, and the web scopes (request/session/application) mirror the natural lifecycles of a web request.

The lifecycle hooks (`@PostConstruct` and `@PreDestroy`) let you plug resource management cleanly into Spring's startup and shutdown sequence — no overriding framework base classes, no static initialisers, just annotated methods. This is the idiomatic, testable approach.

If you're coming from Scala, you'll find the mental model maps well: singletons behave like top-level `given` instances, prototypes like calling a factory function, and the lifecycle callbacks like `Resource.make` — acquire on open, release on close.

## Code Samples

All examples in this post are backed by a **real Spring Framework 7 `ApplicationContext`** (`spring-context` + `spring-web` + `spring-test`) — no simulation:

`java21/src/main/java/io/github/sps23/spring/scopes/`

- [`OrderVolumeTracker.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/OrderVolumeTracker.java) — singleton `@Service` with shared mutable state
- [`ReportBuilder.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/ReportBuilder.java) — `@Component @Scope("prototype")`
- [`StaleReportService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/StaleReportService.java) — reproduces the prototype-in-singleton trap
- [`ReportService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/ReportService.java) — the `ObjectProvider<T>` fix
- [`LookupReportService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/LookupReportService.java) — the `@Lookup` method-injection alternative
- [`ManagedConnectionPool.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/ManagedConnectionPool.java) — `@PostConstruct` / `@PreDestroy`
- [`LegacyCacheManager.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/LegacyCacheManager.java) — `InitializingBean` / `DisposableBean`
- [`EmbeddedMessageBroker.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/EmbeddedMessageBroker.java) — a plain POJO wired with `@Bean(initMethod/destroyMethod)`
- [`RequestTrace.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/RequestTrace.java) — request-scoped bean with a `TARGET_CLASS` proxy
- [`ShoppingCart.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/ShoppingCart.java) — session-scoped bean with a `TARGET_CLASS` proxy
- [`ScopesLifecycleConfig.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/scopes/ScopesLifecycleConfig.java) — `@Configuration` wiring all of the above

`java21/src/test/java/io/github/sps23/spring/scopes/`

- [`SingletonScopeTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/scopes/SingletonScopeTest.java) — proves singleton identity and shared state, via a real `AnnotationConfigApplicationContext`
- [`PrototypeScopeAndInjectionTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/scopes/PrototypeScopeAndInjectionTest.java) — proves the prototype-in-singleton bug, then proves the `ObjectProvider`/`@Lookup` fixes
- [`LifecycleCallbacksTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/scopes/LifecycleCallbacksTest.java) — proves all three lifecycle callback styles fire at context startup/shutdown
- [`WebScopedBeansTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/scopes/WebScopedBeansTest.java) — proves request/session scope behavior using a real `AnnotationConfigWebApplicationContext` plus mock servlet request/session objects

Run the tests yourself with:

```bash
./gradlew :java21:test --tests "io.github.sps23.spring.scopes.*"
```

---

*This post is part of the [Spring Framework Interview Preparation series]({{ site.baseurl }}{% link _posts/2025-12-14-spring-framework-interview-preparation-guide.md %}). Check out the full plan for all Spring topics.*
