---
layout: post
title: "Spring Configuration Approaches: From XML to Java Config"
description: "Learn how to wire Spring applications with @Configuration, @Bean, @ComponentScan, @Profile, and legacy XML — including when XML still earns its keep and what Spring 7 changes compared with Spring 6."
date: 2026-07-26 10:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview]
tags: [java, java21, spring, spring-framework, configuration, xml, annotations, component-scan, profiles]
---

You join a team, open the codebase, and immediately discover three different ways of creating the same `CheckoutService`: one class annotated with `@Service`, one `@Bean` method in a Java config class, and one heroic XML file from 2014 that still wires a partner integration nobody wants to touch before the next release. Welcome to real Spring.

This topic matters in interviews because it reveals whether you understand Spring as a container or whether you only remember a few annotations from muscle memory. If you are coming from Scala, think of Java config as the explicit "module wiring" object you would write by hand, while component scanning is a runtime discovery mechanism that trades some explicitness for convenience.

## The Problem / Context

Spring has supported XML, annotations, and Java configuration for years. The hard part is not memorising syntax; it is knowing **when** to use each style and how they work together without creating a mystery graph that only the container understands.

The questions interviewers usually care about are practical:

1. How does Spring know what a bean is?
2. What is the difference between `@Component` and `@Bean`?
3. How do you swap implementations between `dev` and `prod`?
4. When do you keep XML around instead of deleting it with extreme prejudice?

The short answer is:

1. `@Component` marks the class itself as a candidate for component scanning.
2. `@Bean` marks a factory method inside a `@Configuration` class.
3. `@Profile` activates different beans per environment.
4. XML still shows up when integrating with legacy modules, shared partner config, or old infrastructure that is stable and not worth rewriting today.

## Key Concepts

<div class="table-wrapper">

| Concept | What it does | Best use |
|---|---|---|
| `@Component`, `@Service`, `@Repository` | Marks a class for component scanning | Your own application classes with simple construction |
| `@Configuration` + `@Bean` | Defines beans with Java code | Third-party classes, conditional setup, explicit wiring |
| `@ComponentScan` | Tells Spring where to discover annotated classes | Bootstrapping a module cleanly |
| `@Profile` | Activates selected beans only in chosen environments | `dev`, `test`, `prod`, feature-specific wiring |
| XML `<bean>` | External bean wiring in XML | Legacy config, externalised integration glue, incremental migration |
| `@ImportResource` | Imports XML into Java config | Mixing modern config with legacy beans |

</div>

## The Solution / Implementation

This repository now includes a real Spring Framework **7.0.8** example in `java21/` that uses all of these approaches together.

The heart of the example is a Java config class:

```java
@Configuration
@ComponentScan(basePackageClasses = CheckoutService.class)
@Import(Spring7FeatureRegistrar.class)
@ImportResource("classpath:io/github/sps23/spring/configuration/legacy-support-policy.xml")
public class CheckoutApplicationConfig {

    @Bean
    public Clock invoiceClock() {
        return Clock.fixed(Instant.parse("2026-07-26T09:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    public InvoiceFormatter invoiceFormatter(Clock invoiceClock) {
        return new InvoiceFormatter(invoiceClock, "EUR");
    }

    @Bean
    @Profile("dev")
    public PaymentClient sandboxPaymentClient() {
        return new SandboxPaymentClient("sandbox-terminal");
    }

    @Bean
    @Profile("prod")
    public PaymentClient livePaymentClient() {
        return new LivePaymentClient("merchant-live-42");
    }
}
```

This one class demonstrates four interview-worthy ideas at once:

1. `@ComponentScan` finds your annotated application classes.
2. `@Bean` creates objects that you do **not** want to annotate directly.
3. `@Profile` swaps implementations without `if/else` branches in business code.
4. `@ImportResource` lets you keep one legacy XML bean while the rest of the app moves to Java config.

### `@Component` vs `@Bean`

This distinction is the one interviewers ask about constantly.

```java
@Component
public class TaxCalculator {

    public BigDecimal totalWithVat(BigDecimal subtotal, String countryCode) {
        // business logic
    }
}
```

Use `@Component` (or `@Service` / `@Repository`) when the class itself is part of your application and Spring can just instantiate it directly.

```java
@Bean
public InvoiceFormatter invoiceFormatter(Clock invoiceClock) {
    return new InvoiceFormatter(invoiceClock, "EUR");
}
```

Use `@Bean` when:

1. the class comes from a library,
2. construction needs explicit arguments,
3. you want setup code in one visible place, or
4. you need environment-specific factory logic.

In the example, `TaxCalculator` is discovered by scanning, while `InvoiceFormatter` and `PaymentClient` are created explicitly by `@Bean` methods.

### Component Scanning with Constructor Injection

The service itself stays boring on purpose, because boring Spring code is usually the best Spring code:

```java
@Service
public class CheckoutService {

    private final PaymentClient paymentClient;
    private final TaxCalculator taxCalculator;
    private final InvoiceFormatter invoiceFormatter;
    private final LegacySupportPolicy legacySupportPolicy;

    public CheckoutService(
            PaymentClient paymentClient,
            TaxCalculator taxCalculator,
            InvoiceFormatter invoiceFormatter,
            LegacySupportPolicy legacySupportPolicy) {
        this.paymentClient = paymentClient;
        this.taxCalculator = taxCalculator;
        this.invoiceFormatter = invoiceFormatter;
        this.legacySupportPolicy = legacySupportPolicy;
    }
}
```

This is still the best practice in Spring 6 and Spring 7:

1. constructor injection for mandatory dependencies,
2. `final` fields for immutability,
3. no field injection,
4. no calling `new` inside business services.

## Profile-Specific Configuration

Profiles are Spring's built-in answer to "How do I activate different configurations for different environments?"

In tests, the example activates profiles programmatically:

```java
var context = new AnnotationConfigApplicationContext();
context.getEnvironment().setActiveProfiles("prod");
context.register(CheckoutApplicationConfig.class);
context.refresh();
```

That causes Spring to create the `livePaymentClient()` bean and skip `sandboxPaymentClient()`.

In a real application you would more often activate profiles with:

1. environment variables,
2. JVM system properties,
3. `application.properties` / `application.yml` in Spring Boot,
4. test annotations such as `@ActiveProfiles`.

The key design rule is simple: **put environment switching in configuration, not in business code**.

## Mixing Java Config and XML

Here is the imported XML bean from the example:

```xml
<bean id="legacySupportPolicy"
    class="io.github.sps23.spring.configuration.LegacySupportPolicy">
    <constructor-arg value="xml-partner-ops" />
    <constructor-arg value="xml-general-support" />
</bean>
```

That bean is then injected into the Java-configured `CheckoutService` exactly like any other dependency. Spring does not care whether a bean came from annotations, Java config, or XML once it is inside the `ApplicationContext`.

This is the real migration story in many companies:

1. keep stable XML-defined infrastructure or partner beans,
2. move new application logic to `@Configuration` and stereotype annotations,
3. delete XML only when the surrounding system is ready.

There is also a pure XML context in the codebase:

```xml
<bean id="checkoutService" class="io.github.sps23.spring.configuration.CheckoutService">
    <constructor-arg ref="paymentClient" />
    <constructor-arg ref="taxCalculator" />
    <constructor-arg ref="invoiceFormatter" />
    <constructor-arg ref="legacySupportPolicy" />
</bean>
```

It works, but it is obviously more verbose and much less refactoring-friendly than the Java config version.

## Spring 7 vs Spring 6

For the core interview topic, the most important thing to understand is that **everyday annotation-based configuration looks almost the same in Spring 6 and Spring 7**. If you already know `@Configuration`, `@Bean`, `@ComponentScan`, and `@Profile` from Spring 6, you do not need to relearn the model.

What Spring 7 adds here is a cleaner first-class story for programmatic registration with `BeanRegistrar`.

```java
public final class Spring7FeatureRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        registry.registerBean(Spring7FeatureNote.class, spec -> spec
                .description("Spring 7 BeanRegistrar example")
                .supplier(context -> new Spring7FeatureNote(
                        env.matchesProfiles("prod")
                                ? "Registered with BeanRegistrar for the production profile"
                                : "Registered with BeanRegistrar for the development profile")));
    }
}
```

<div class="table-wrapper">

| Topic | Spring 6 style | Spring 7 style |
|---|---|---|
| `@Configuration` / `@Bean` / `@Profile` | Standard and still recommended | Same model, still recommended |
| Mixing XML with Java config | Supported | Supported |
| Programmatic bean registration | Usually lower-level container APIs or custom infrastructure code | First-class `BeanRegistrar` support |
| AOT-friendly programmatic registration | More manual | Built into the `BeanRegistrar` story |

</div>

So if an interviewer asks "What changes in Spring 7?", a good answer is:

1. the day-to-day configuration model stays stable,
2. Spring 7 keeps the annotation and Java config approach you already know,
3. and it adds a cleaner programmatic registration API for advanced cases.

## When XML Still Makes Sense

XML is not the default choice for new code, but pretending it never has a place is the kind of absolutism that breaks migrations.

XML can still be reasonable when:

1. a legacy module already ships XML bean definitions that are stable,
2. a platform team shares external wiring across several applications,
3. you are migrating incrementally and want to avoid rewriting everything in one risky commit,
4. non-Java teams need to tweak wiring without recompiling code.

What you should **not** do is start a greenfield Spring 7 application with hundreds of XML bean definitions just because somebody once did that in Spring 3.

## Best Practices

1. Prefer constructor injection for required dependencies.
2. Use `@Component` for straightforward application classes; use `@Bean` for explicit or third-party construction.
3. Keep profile differences in configuration classes, not in `if (prod)` branches.
4. Use `@ImportResource` only as a bridge when XML still has real value.
5. Keep configuration modules small and cohesive instead of one giant `AppConfig`.
6. In Spring 7, reach for `BeanRegistrar` only when plain `@Bean` methods stop being expressive enough.

## Conclusion

Spring configuration is not a battle between "annotations good, XML bad." It is a question of choosing the most maintainable wiring style for the code you have today while keeping migrations boring and safe.

For most modern applications, the answer is Java config plus stereotype annotations, with constructor injection and profile-based wiring. XML survives as a compatibility layer, and in Spring 7 you also get `BeanRegistrar` when you need more advanced programmatic registration without dropping into lower-level container plumbing.

## Code Samples

All examples in this post are runnable. Find them in the repository:

- [Java 21 Spring configuration examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/spring/configuration)
- [Java 21 Spring configuration tests](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/test/java/io/github/sps23/spring/configuration)
- [Legacy XML resources](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/resources/io/github/sps23/spring/configuration)
- [Java 21 Gradle module dependencies](https://github.com/sps23/java-for-scala-devs/blob/main/java21/build.gradle)

---

*This post is part of the [Spring Framework Interview Preparation Guide - Master the Framework]({{ site.baseurl }}{% link _posts/2025-12-14-spring-framework-interview-preparation-guide.md %}). Next related post: [Building RESTful APIs with Spring MVC]({{ site.baseurl }}{% link _posts/2026-05-28-spring-mvc-restful-apis.md %}).*
