---
layout: post
title: "Building RESTful APIs with Spring MVC"
description: "A practical guide to Spring MVC REST controllers — @RestController, HTTP method mappings, path variables, request/response handling, exception handling with @ControllerAdvice, and @Valid validation — with investment trading examples."
date: 2026-05-28 14:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview]
tags: [java, java21, spring, spring-boot, spring-mvc, rest-api, interview-preparation]
---

Your investments may be tanking — down 40% since Tuesday because you bought something called "MoonCoinX" — but at least your REST API is well-structured. In this post we will build a trading API with Spring MVC, and by the end you will know exactly how to answer every Spring MVC interview question thrown at you.

If you are coming from Scala, think of Spring MVC as the HTTP routing layer: you define functions that map URL patterns to logic, and Spring handles all the serialization, deserialization, and HTTP plumbing around them. In Scala you might use Play Framework or http4s routes; Spring MVC is the Java equivalent — annotation-heavy but surprisingly predictable once you understand the model.

Let's build a portfolio management API — one that lets traders buy stocks, sell them at the wrong time, and watch their net worth slowly evaporate in real-time.

## @Controller vs @RestController — The First Interview Question

Every Spring MVC interview starts here. The difference is smaller than you think:

```java
// @Controller — returns a view name (HTML templating with Thymeleaf, JSP, etc.)
@Controller
public class PortfolioViewController {

    @GetMapping("/portfolio")
    public String showPortfolio(Model model) {
        model.addAttribute("positions", tradeService.getPortfolio());
        return "portfolio";  // resolves to portfolio.html template
    }
}

// @RestController — returns data, serialized to JSON (or XML) automatically
// @RestController = @Controller + @ResponseBody on every method
@RestController
public class TradeController {

    @GetMapping("/api/trades")
    public List<Trade> getAllTrades() {
        return tradeService.getTrades(Optional.empty());
        // Spring + Jackson serializes this to JSON — no @ResponseBody needed
    }
}
```

**The key insight:** `@RestController` adds `@ResponseBody` to every handler method, which tells Spring to write the return value directly to the HTTP response body (via `HttpMessageConverter`/Jackson) instead of looking it up as a view name.

In the REST world you almost always want `@RestController`. Use plain `@Controller` only when you are rendering server-side HTML templates.

<div class="table-wrapper" markdown="1">

| Annotation | What it does | Returns |
|---|---|---|
| `@Controller` | Marks class as an MVC controller | View name (String → template) |
| `@RestController` | `@Controller` + `@ResponseBody` | Serialised data (JSON, XML) |
| `@ResponseBody` | Write return value to HTTP body | Applied per-method or via `@RestController` |

</div>

## HTTP Method Mapping Annotations

Spring MVC provides a shortcut annotation for every HTTP verb. In an interview, you should know all of them and when to use each.

```java
@RestController
@RequestMapping("/api/trades")  // base path for all methods in this controller
public class TradeController {

    private final TradeService tradeService;

    // Constructor injection — Spring injects the registered TradeService bean
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // GET /api/trades  — list all trades (or filter by symbol)
    @GetMapping
    public List<Trade> getTrades(
            @RequestParam(required = false) String symbol) {
        return tradeService.getTrades(Optional.ofNullable(symbol));
    }

    // GET /api/trades/{tradeId}  — get a specific trade
    @GetMapping("/{tradeId}")
    public Trade getTradeById(@PathVariable String tradeId) {
        return tradeService.getTradeById(tradeId);  // throws TradeNotFoundException if absent
    }

    // POST /api/trades  — execute a new trade
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // respond with 201 instead of default 200
    public Trade executeTrade(@RequestBody @Valid TradeRequest request) {
        return tradeService.executeTrade(request);
    }

    // DELETE /api/trades/{tradeId}  — cancel a pending trade
    @DeleteMapping("/{tradeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // 204 — success with no body
    public void cancelTrade(@PathVariable String tradeId) {
        tradeService.cancelTrade(tradeId);
    }
}
```

**@RequestMapping vs @GetMapping:** `@RequestMapping` is the meta-annotation; `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, and `@DeleteMapping` are composed shortcuts that set the `method` attribute for you. They were added in Spring 4.3 because `@RequestMapping(method = RequestMethod.GET)` is a lot to type 40 times a day.

<div class="table-wrapper" markdown="1">

| Annotation | HTTP Verb | Typical use | Typical status |
|---|---|---|---|
| `@GetMapping` | GET | Read / list resources | 200 OK |
| `@PostMapping` | POST | Create a new resource | 201 Created |
| `@PutMapping` | PUT | Replace a resource | 200 OK |
| `@PatchMapping` | PATCH | Partially update a resource | 200 OK |
| `@DeleteMapping` | DELETE | Remove a resource | 204 No Content |

</div>

## Path Variables and Request Parameters

Two of the most common interview questions rolled into one section.

### @PathVariable — Part of the URL path

```java
// URL: GET /api/trades/trade-abc-123
@GetMapping("/{tradeId}")
public Trade getTradeById(@PathVariable String tradeId) {
    // tradeId = "trade-abc-123"
    return tradeService.getTradeById(tradeId);
}

// Multiple path variables
// URL: GET /api/portfolios/john/positions/AAPL
@GetMapping("/portfolios/{owner}/positions/{symbol}")
public PortfolioPosition getPosition(
        @PathVariable String owner,
        @PathVariable String symbol) {
    return portfolioService.getPosition(owner, symbol);
}
```

### @RequestParam — Query string parameters

```java
// URL: GET /api/trades?symbol=AAPL&type=BUY
@GetMapping
public List<Trade> getTrades(
        @RequestParam(required = false) String symbol,
        @RequestParam(required = false) TradeType type) {
    return tradeService.getTrades(Optional.ofNullable(symbol), Optional.ofNullable(type));
}

// With defaults — URL: GET /api/trades?page=0&size=20
@GetMapping
public Page<Trade> getTrades(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return tradeService.getTrades(PageRequest.of(page, size));
}
```

**The interview rule:** path variables identify *which resource* you want; query parameters *filter or modify* what you get back. `GET /api/trades/trade-123` (path) retrieves a specific trade; `GET /api/trades?symbol=AAPL` (query param) filters the collection.

### @RequestBody — The POST payload

```java
// POST body: {"symbol":"AAPL","type":"BUY","quantity":10,"pricePerShare":"182.50"}
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Trade executeTrade(@RequestBody @Valid TradeRequest request) {
    // Spring + Jackson deserialized the JSON into a TradeRequest record for us
    return tradeService.executeTrade(request);
}
```

Jackson automatically deserializes the JSON body into a `TradeRequest` record. No XML configuration, no `ObjectMapper` setup — Spring Boot's auto-configuration handles it.

## Exception Handling — @ExceptionHandler and @ControllerAdvice

This is where many Spring MVC interviews get interesting. You have two levels of exception handling:

### Level 1: @ExceptionHandler — handler method scope

```java
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    // Handles TradeNotFoundException only within THIS controller
    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(TradeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    @GetMapping("/{tradeId}")
    public Trade getTradeById(@PathVariable String tradeId) {
        return tradeService.getTradeById(tradeId);  // throws TradeNotFoundException
        // Spring calls handleNotFound() and returns a clean 404 JSON response
    }
}
```

### Level 2: @ControllerAdvice — global scope (the right approach)

```java
// Applies to ALL controllers in the application
@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(TradeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEverythingElse(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "An unexpected error occurred"));
    }
}
```

The `ApiError` record in our codebase captures this pattern:

```java
public record ApiError(int status, String message, Instant timestamp) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Instant.now());
    }
}
```

A `TradeNotFoundException` response looks like:

```json
{
  "status": 404,
  "message": "Trade trade-999 not found",
  "timestamp": "2025-12-15T14:00:00Z"
}
```

**Interview tip:** Always reach for `@ControllerAdvice` over per-controller `@ExceptionHandler`. Central exception handling means consistent error responses across all endpoints, and you write the handler once. The per-controller version is only useful when one controller genuinely needs different error behaviour from the rest.

## Request Validation with @Valid

Spring integrates with Bean Validation (JSR-380 / Hibernate Validator) to validate request bodies before they reach your handler.

```java
// TradeRequest.java — annotated for Bean Validation
public record TradeRequest(
        @NotBlank(message = "symbol must not be blank")
        String symbol,

        @NotNull(message = "type must be BUY or SELL")
        TradeType type,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity,

        @NotNull
        @DecimalMin(value = "0.01", message = "pricePerShare must be positive")
        BigDecimal pricePerShare) {
}
```

Add `@Valid` to your handler parameter and Spring will run the validators automatically:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Trade executeTrade(@RequestBody @Valid TradeRequest request) {
    // If any @NotBlank / @Min / @DecimalMin fails, Spring throws
    // MethodArgumentNotValidException before this line is reached.
    // Your @ControllerAdvice handler turns that into a 400 response.
    return tradeService.executeTrade(request);
}
```

Without `@Valid`, the annotations are decorative — Spring ignores them. Always pair `@Valid` with constraint annotations.

The full validation flow:

```
POST /api/trades  →  Jackson deserializes JSON  →  @Valid runs validators
    ↓ valid                                          ↓ invalid
handler method                           MethodArgumentNotValidException
    ↓                                          ↓
201 Created                         @ControllerAdvice → 400 Bad Request
```

## HTTP Status Codes — The Cheat Sheet

Spring MVC gives you several ways to control the response status:

```java
// Option 1: @ResponseStatus on the method
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 201
public Trade executeTrade(@RequestBody @Valid TradeRequest request) { ... }

// Option 2: ResponseEntity — full control over status + headers + body
@GetMapping("/{tradeId}")
public ResponseEntity<Trade> getTradeById(@PathVariable String tradeId) {
    var trade = tradeService.getTradeById(tradeId);
    return ResponseEntity.ok(trade);                  // 200 with body
    // or: return ResponseEntity.status(HttpStatus.OK).body(trade);
}

// Option 3: ResponseEntity for conditional responses
@PostMapping("/{tradeId}/cancel")
public ResponseEntity<Void> cancelTrade(@PathVariable String tradeId) {
    boolean cancelled = tradeService.cancelTrade(tradeId);
    return cancelled
            ? ResponseEntity.noContent().build()      // 204 — trade cancelled
            : ResponseEntity.notFound().build();       // 404 — trade not found
}
```

<div class="table-wrapper" markdown="1">

| Status | When to return it |
|---|---|
| 200 OK | Successful GET, PUT, PATCH |
| 201 Created | Successful POST that created a resource |
| 204 No Content | Successful DELETE or action with no response body |
| 400 Bad Request | Invalid input, failed validation |
| 401 Unauthorized | Not authenticated |
| 403 Forbidden | Authenticated but lacks permission |
| 404 Not Found | Resource does not exist |
| 409 Conflict | Resource already exists; duplicate trade |
| 422 Unprocessable Entity | Semantically invalid request (business rule violation) |
| 500 Internal Server Error | Unexpected failure |

</div>

## Putting It Together — The Full Trade Controller

Here is what the complete controller looks like with all the patterns wired up:

```java
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // GET /api/trades          — list all
    // GET /api/trades?symbol=AAPL  — filtered by symbol
    @GetMapping
    public List<Trade> getTrades(
            @RequestParam(required = false) String symbol) {
        return tradeService.getTrades(Optional.ofNullable(symbol));
    }

    // GET /api/trades/{tradeId}
    @GetMapping("/{tradeId}")
    public Trade getTradeById(@PathVariable String tradeId) {
        return tradeService.getTradeById(tradeId);
    }

    // POST /api/trades
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trade executeTrade(@RequestBody @Valid TradeRequest request) {
        return tradeService.executeTrade(request);
    }
}

// Separate controller — different resource
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final TradeService tradeService;

    public PortfolioController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // GET /api/portfolio — full portfolio snapshot
    @GetMapping
    public PortfolioSummary getPortfolio() {
        return tradeService.getPortfolio();
    }
}
```

The service does all the work; the controllers handle HTTP concerns only. This is the separation of concerns Spring MVC is designed around.

## Interview Questions — Answered

### "What's the difference between @Controller and @RestController?"

`@RestController` is a composed annotation: `@Controller` + `@ResponseBody`. The `@ResponseBody` on every method tells Spring to serialise return values to the HTTP response body (JSON by default via Jackson) instead of treating them as view names. Use `@RestController` for REST APIs; use `@Controller` when you are rendering HTML templates.

### "How do you handle path variables and query parameters?"

- `@PathVariable` extracts segments from the URL path: `GET /trades/{id}` → `@PathVariable String id`
- `@RequestParam` extracts query string parameters: `GET /trades?symbol=AAPL` → `@RequestParam String symbol`

Path variables identify which resource; query parameters filter or shape what you get back.

### "What's the difference between @RequestMapping and @GetMapping?"

`@RequestMapping` is the general-purpose annotation with a `method` attribute. `@GetMapping` is a shortcut for `@RequestMapping(method = RequestMethod.GET)`. The specialised forms (`@GetMapping`, `@PostMapping`, etc.) were added in Spring 4.3 for readability. Prefer them in new code.

### "How do you handle exceptions in Spring MVC? What's @ControllerAdvice?"

`@ExceptionHandler` methods catch specific exceptions and return custom responses. When placed inside a `@ControllerAdvice` (or `@RestControllerAdvice`) class, they apply globally across all controllers. This keeps exception-handling logic centralised and consistent — one place to define your 404 response, one place to define your 400 response, one place to define your 500 response.

### "How do you validate request bodies in Spring? What role does @Valid play?"

Bean Validation annotations (`@NotBlank`, `@Min`, `@DecimalMin`, etc.) declare constraints on your request model. They are inert unless you trigger validation with `@Valid` (or `@Validated`) on the handler parameter. When validation fails, Spring throws `MethodArgumentNotValidException`, which your `@ControllerAdvice` should catch and convert to a 400 response with descriptive field-level error messages.

## Comparison Table

<div class="table-wrapper" markdown="1">

| Concept | Spring MVC (Java) | Play Framework (Scala) | Ktor (Kotlin) |
|---|---|---|---|
| Controller annotation | `@RestController` | `extends BaseController` | N/A — routing DSL |
| Route mapping | `@GetMapping("/path")` | `def index = Action { ... }` + routes file | `get("/path") { ... }` |
| Path variable | `@PathVariable String id` | `def show(id: Long) = ...` | `call.parameters["id"]` |
| Query param | `@RequestParam String q` | `request.getQueryString("q")` | `call.request.queryParameters["q"]` |
| Request body | `@RequestBody @Valid T body` | `request.body.asJson` | `call.receive<T>()` |
| Exception handling | `@ControllerAdvice` class | `onError` in `HttpErrorHandler` | `StatusPages` plugin |
| Validation | Bean Validation + `@Valid` | Form binding + constraints | Valiktor / manual |

</div>

## Code Samples

All examples in this post are backed by runnable plain-Java code in the repository.
The service layer contains no Spring dependencies — it can be tested with plain JUnit 5,
without starting a Spring application context:

`java21/src/main/java/io/github/sps23/spring/mvc/`

- [`Trade.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/Trade.java) — domain record: tradeId, symbol, type, quantity, price, timestamp
- [`TradeType.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/TradeType.java) — BUY / SELL enum
- [`TradeRequest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/TradeRequest.java) — POST body record (with validation comments)
- [`PortfolioSummary.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/PortfolioSummary.java) — portfolio snapshot with positions and gain/loss
- [`ApiError.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/ApiError.java) — standard error response body
- [`TradeNotFoundException.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/TradeNotFoundException.java) — maps to 404 via `@ExceptionHandler`
- [`TradeService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/TradeService.java) — service interface (injected into controller)
- [`InMemoryTradeService.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/spring/mvc/InMemoryTradeService.java) — testable in-memory implementation

`java21/src/test/java/io/github/sps23/spring/mvc/`

- [`InMemoryTradeServiceTest.java`](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/spring/mvc/InMemoryTradeServiceTest.java) — JUnit 5 tests covering all service operations

Run the tests yourself with:

```bash
./gradlew :java21:test --tests "io.github.sps23.spring.mvc.*"
```

---

*This post is part of the [Spring Framework Interview Preparation Guide - Master the Framework]({{ site.baseurl }}{% link _posts/2025-12-14-spring-framework-interview-preparation-guide.md %}). Nearby related posts from the same guide: [Spring Configuration Approaches: From XML to Java Config]({{ site.baseurl }}{% link _posts/2026-07-26-spring-configuration-approaches.md %}) and [Spring Bean Scopes and Lifecycle Management]({{ site.baseurl }}{% link _posts/2026-05-25-spring-bean-scopes-lifecycle.md %}).*
