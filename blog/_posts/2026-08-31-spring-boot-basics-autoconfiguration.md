---
layout: post
title: "Spring Boot Basics and Auto-Configuration"
description: "Learn how Spring Boot removes boilerplate with @SpringBootApplication, auto-configuration, and externalised application properties, plus how to override the defaults when you need to."
date: 2026-08-31 11:00:00 +0000
updated: 2026-08-31 11:00:00 +0000
categories: [interview]
tags: [java, java21, spring, spring-boot, auto-configuration, interview-preparation]
---

You open a new Spring project and somehow it already has a web server, JSON support, logging, and a sensible app structure without you wiring every bean by hand. That is the Spring Boot trick: it starts from opinionated defaults, then lets you override the parts you actually care about.

If you are coming from Scala, think of Spring Boot as the difference between writing every module wiring manually and starting from a well-stocked application template. You still control the pieces, but the framework does the boring setup work first.

## The Problem / Context

Plain Spring is powerful, but it can feel like a lot of ceremony for a small service. You often need to decide which dependencies to include, how to wire the application, how to configure embedded servers, and where to keep environment-specific settings.

Spring Boot solves that by:

1. guessing sensible defaults from the classpath,
2. wiring common infrastructure automatically,
3. giving you a single entry point for startup,
4. and letting you override defaults only when needed.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | What it does | Why it matters |
|---|---|---|
| `@SpringBootApplication` | Combines several core Spring Boot annotations | Gives you a clean application entry point |
| Auto-configuration | Creates beans based on what is on the classpath | Removes repetitive setup code |
| Starter dependencies | Curated dependency bundles like web, test, JPA | Avoids version and dependency sprawl |
| `application.properties` / `application.yml` | Externalised configuration | Keeps settings out of code |
| `@ConfigurationProperties` | Binds configuration to typed objects | Safer than scattering `@Value` everywhere |
| Auto-configuration overrides | Exclude or replace Boot defaults | Lets you customize when defaults are not enough |

</div>

## The Solution / Implementation

At the center of every Boot app is the main class:

```java
@SpringBootApplication
public class SpringBootBasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBasicsApplication.class, args);
    }
}
```

`@SpringBootApplication` is a convenience annotation. It combines:

1. `@Configuration`
2. `@ComponentScan`
3. `@EnableAutoConfiguration`

That last one is the important part. It tells Spring Boot to inspect the classpath and create common beans automatically. If Spring MVC is present, Boot configures web infrastructure. If Jackson is present, Boot configures JSON support. If you add a data module, Boot starts looking for repository support.

Here is the basic idea in code:

```java
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot");
    }
}
```

You do not configure the dispatcher servlet, JSON mapper, or embedded server manually. Boot does that for you as long as the right starter dependency is present.

### A Small Real Configuration Example

The same Boot idea shows up in a tiny application: one `@SpringBootApplication` class, one controller, and one typed configuration object.

```java
@SpringBootApplication
public class SpringBootBasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBasicsApplication.class, args);
    }
}
```

```java
@RestController
@RequestMapping("/api/greeting")
public class GreetingController {

    private final AppProperties properties;

    public GreetingController(AppProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public Map<String, String> greeting() {
        return Map.of("message", properties.greeting());
    }
}
```

```java
@ConfigurationProperties(prefix = "app")
public record AppProperties(String greeting) {
}
```

## Auto-Configuration in Practice

Auto-configuration is not magic. It is a set of conditional configuration classes that activate when certain classes, beans, or properties are present.

```java
@Configuration(proxyBeanMethods = false)
public class CustomGreetingConfig {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService("Spring Boot developer");
    }
}
```

Boot will keep out of the way when you define your own bean. That is the usual rule: Boot provides a default only when you do not already have one.

If you want to change a default, you typically do one of three things:

1. define your own bean,
2. set a property in `application.properties` or `application.yml`,
3. or exclude an auto-configuration class.

Example:

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ApplicationWithoutDatabaseAutoConfig {
}
```

That is useful when a starter brings in a feature you do not want yet.

Another common pattern is overriding a Boot-managed bean with your own implementation:

```java
@Configuration
public class JsonMessageConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
```

When Spring Boot sees this bean, it uses it instead of creating its own default mapper.

## Externalised Configuration

Spring Boot makes configuration a first-class feature. Instead of hard-coding values, keep them in property files:

```properties
server.port=8081
app.greeting=Hello, Scala developer
spring.profiles.active=dev
```

Then bind them into a type-safe object:

```java
@ConfigurationProperties(prefix = "app")
public record AppProperties(String greeting) {
}
```

This is usually better than scattering `@Value` across a codebase. It groups related settings together and gives you a clearer shape to validate and test.

## Interview Q&A

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What is Spring Boot?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Spring Boot is Spring with opinionated defaults and auto-configuration. It removes a lot of setup work so you can start with a working application faster. In practice, that means less XML, less manual wiring, and fewer framework decisions before you write business code.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>What does auto-configuration do?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Auto-configuration creates common beans automatically based on what is available on the classpath and in your configuration. If you already define your own bean, Boot backs off. That keeps the default behavior useful without making the app rigid.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>What does @SpringBootApplication include?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It combines `@Configuration`, `@ComponentScan`, and `@EnableAutoConfiguration`. That gives you a single main class that can start the application, discover your beans, and enable Boot defaults. Interviewers like this question because it checks whether you know what Boot is really doing for you.
    </div>
  </details>
</div>

## Conclusion

Spring Boot does not replace Spring; it makes Spring easier to start and easier to operate. If you remember one thing, remember this: Boot gives you defaults, but you still control the final wiring when the defaults are not right.

## Code Samples

All examples in this post are runnable in the repository:
- [Spring Boot basics classes](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/spring/boot)
- [Spring Boot configuration examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/spring/configuration)
- [Spring Boot configuration tests](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/test/java/io/github/sps23/spring/configuration)

---

*This is part of our [Spring Framework Interview Preparation Guide - Master the Framework]({{ site.baseurl }}{% link _posts/2025-12-14-spring-framework-interview-preparation-guide.md %}).*
