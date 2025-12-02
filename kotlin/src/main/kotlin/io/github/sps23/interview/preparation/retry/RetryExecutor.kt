package io.github.sps23.interview.preparation.retry

import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

/**
 * Retry context holding information about the current retry state.
 *
 * @param attempt current attempt number (1-based)
 * @param startTime when the first attempt started
 * @param lastError exception from the most recent failed attempt
 */
data class RetryContext(
    val attempt: Int,
    val startTime: Instant,
    val lastError: Throwable,
) {
    /** Creates a new context for the next retry attempt. */
    fun nextAttempt(newError: Throwable): RetryContext = copy(attempt = attempt + 1, lastError = newError)

    /** Gets the elapsed time since the first attempt. */
    fun elapsed(): Duration = Duration.between(startTime, Instant.now())

    companion object {
        /** Creates a new context for the first failed attempt. */
        fun initial(error: Throwable): RetryContext = RetryContext(1, Instant.now(), error)
    }
}

/**
 * Represents the result of a retry operation.
 *
 * Comparison with Java:
 * ```java
 * // Java uses sealed interface with record implementations
 * sealed interface RetryResult<T> {
 *     record Success<T>(T value, int attempts) implements RetryResult<T> {}
 *     record Failure<T>(Throwable error, int attempts) implements RetryResult<T> {}
 * }
 * ```
 *
 * Comparison with Scala:
 * ```scala
 * enum RetryResult[+T]:
 *   case Success(value: T, attempts: Int)
 *   case Failure(error: Throwable, attempts: Int)
 * ```
 */
sealed class RetryResult<out T> {
    abstract val attempts: Int

    abstract fun isSuccess(): Boolean

    data class Success<T>(
        val value: T,
        override val attempts: Int,
    ) : RetryResult<T>() {
        override fun isSuccess() = true
    }

    data class Failure<T>(
        val error: Throwable,
        override val attempts: Int,
    ) : RetryResult<T>() {
        override fun isSuccess() = false
    }

    companion object {
        fun <T> success(
            value: T,
            attempts: Int,
        ): RetryResult<T> = Success(value, attempts)

        fun <T> failure(
            error: Throwable,
            attempts: Int,
        ): RetryResult<T> = Failure(error, attempts)
    }
}

/** Custom exception for retry-related failures. */
class RetryException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Type aliases demonstrating Kotlin's approach to function types.
 *
 * Comparison with Java and Scala:
 * ```java
 * // Java requires @FunctionalInterface annotation
 * @FunctionalInterface
 * interface RetryPolicy {
 *     Duration delayFor(int attempt, Throwable error);
 * }
 * ```
 * ```scala
 * // Scala uses type aliases
 * type RetryPolicy = (Int, Throwable) => Duration
 * ```
 * ```kotlin
 * // Kotlin uses typealias with function types
 * typealias RetryPolicy = (Int, Throwable) -> Duration
 * ```
 */
typealias RetryPolicy = (Int, Throwable) -> Duration

typealias RetryCondition = (RetryContext) -> Boolean

typealias RetryListener = (RetryContext, Duration) -> Unit

/**
 * Retry policy factory functions - equivalent to Java's RetryPolicy interface.
 */
object RetryPolicies {
    /**
     * Creates a fixed delay policy.
     *
     * Comparison with Java:
     * ```java
     * static RetryPolicy fixed(Duration delay) {
     *     return (attempt, error) -> delay;
     * }
     * ```
     */
    fun fixed(delay: Duration): RetryPolicy = { _, _ -> delay }

    /**
     * Creates an exponential backoff policy.
     *
     * Demonstrates lambda with calculation logic, same as Java.
     */
    fun exponentialBackoff(
        initialDelay: Duration,
        maxDelay: Duration,
    ): RetryPolicy =
        { attempt, _ ->
            val delayMs = initialDelay.toMillis() * Math.pow(2.0, (attempt - 1).toDouble()).toLong()
            Duration.ofMillis(minOf(delayMs, maxDelay.toMillis()))
        }

    /**
     * Creates a policy that varies delay based on exception type.
     */
    fun errorSpecific(
        networkDelay: Duration,
        otherDelay: Duration,
    ): RetryPolicy =
        { _, error ->
            when (error) {
                is IOException -> networkDelay
                else -> otherDelay
            }
        }
}

/**
 * Extension function for RetryPolicy composition.
 *
 * Comparison with Java:
 * ```java
 * // Java uses default methods in the interface
 * default RetryPolicy maxWith(RetryPolicy other) { ... }
 * ```
 */
fun RetryPolicy.maxWith(other: RetryPolicy): RetryPolicy =
    { attempt, error ->
        val selfDelay = this(attempt, error)
        val otherDelay = other(attempt, error)
        if (selfDelay > otherDelay) selfDelay else otherDelay
    }

/**
 * Retry condition factory functions - equivalent to Java's RetryCondition interface.
 */
object RetryConditions {
    /**
     * Creates a condition that limits retry attempts.
     */
    fun maxAttempts(max: Int): RetryCondition = { ctx -> ctx.attempt < max }

    /**
     * Creates a condition based on exception type.
     *
     * Comparison with Java:
     * ```java
     * @SafeVarargs
     * static RetryCondition forExceptions(Class<? extends Throwable>... types) { ... }
     * ```
     */
    fun forExceptions(vararg types: KClass<out Throwable>): RetryCondition = { ctx -> types.any { it.isInstance(ctx.lastError) } }

    /**
     * Creates a condition based on exception type using reified generic.
     *
     * This is a Kotlin-specific feature that Java doesn't have.
     */
    inline fun <reified E : Throwable> forException(): RetryCondition = { ctx -> ctx.lastError is E }

    /**
     * Creates a condition that checks exception message.
     */
    fun messageContains(pattern: String): RetryCondition = { ctx -> ctx.lastError.message?.contains(pattern) == true }
}

/**
 * Extension functions for RetryCondition composition.
 *
 * Comparison with Java:
 * ```java
 * default RetryCondition and(RetryCondition other) {
 *     return ctx -> this.shouldRetry(ctx) && other.shouldRetry(ctx);
 * }
 * ```
 *
 * Kotlin allows infix functions for more readable syntax:
 * ```kotlin
 * condition1 and condition2   // instead of condition1.and(condition2)
 * ```
 */
infix fun RetryCondition.and(other: RetryCondition): RetryCondition = { ctx -> this(ctx) && other(ctx) }

infix fun RetryCondition.or(other: RetryCondition): RetryCondition = { ctx -> this(ctx) || other(ctx) }

operator fun RetryCondition.not(): RetryCondition = { ctx -> !this(ctx) }

/**
 * A configurable retry executor demonstrating functional programming in Kotlin.
 *
 * This class showcases key Kotlin functional programming concepts:
 *
 * - Function types (typealias) vs Java's @FunctionalInterface
 * - Extension functions for composition
 * - Infix functions for readable DSL
 * - Reified generics for type checking
 * - Trailing lambda syntax
 * - Higher-order functions
 *
 * Comparison with Java:
 * ```java
 * var executor = RetryExecutor.<String>builder()
 *     .withPolicy(RetryPolicy.exponentialBackoff(...))
 *     .withCondition(RetryCondition.maxAttempts(3))
 *     .withRetryListener(event -> log(event))
 *     .build();
 * ```
 *
 * Kotlin version (more concise):
 * ```kotlin
 * val executor = RetryExecutor<String>(
 *     policy = RetryPolicies.exponentialBackoff(...),
 *     condition = RetryConditions.maxAttempts(3),
 *     onRetry = { ctx, delay -> log(ctx, delay) }
 * )
 * ```
 *
 * @param policy function determining delay between retries
 * @param condition function determining whether to retry
 * @param onRetry callback invoked before each retry
 * @param transformer function to transform successful results
 */
class RetryExecutor<T>(
    private val policy: RetryPolicy = RetryPolicies.fixed(Duration.ofSeconds(1)),
    private val condition: RetryCondition = RetryConditions.maxAttempts(3),
    private val onRetry: RetryListener = { _, _ -> },
    private val transformer: (T) -> T = { it },
) {
    /**
     * Executes an operation with retry logic.
     *
     * Kotlin's trailing lambda syntax makes this very clean:
     * ```kotlin
     * executor.execute {
     *     fetchData()
     * }
     * ```
     *
     * @param operation the operation to execute (lambda parameter)
     * @return the retry result
     */
    fun execute(operation: () -> T): RetryResult<T> {
        var attempt = 0
        var context: RetryContext? = null

        while (true) {
            attempt++
            try {
                val result = operation()
                // Apply transformation
                val transformed = transformer(result)
                return RetryResult.success(transformed, attempt)
            } catch (e: Throwable) {
                context =
                    context?.nextAttempt(e) ?: RetryContext.initial(e)

                // Check retry condition
                if (!condition(context)) {
                    return RetryResult.failure(e, attempt)
                }

                // Calculate delay
                val delay = policy(attempt, e)

                // Notify listener
                onRetry(context, delay)

                // Wait before retry
                sleep(delay)
            }
        }
    }

    private fun sleep(delay: Duration) {
        try {
            Thread.sleep(delay.toMillis())
        } catch (ie: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RetryException("Retry interrupted", ie)
        }
    }
}

/**
 * Demonstrates method/function references comparison between Kotlin, Java, and Scala.
 */
object MethodReferencesDemo {
    /**
     * Demonstrates various function references and their Java equivalents.
     *
     * ```java
     * // Java method references:
     * Function<Long, Duration> staticRef = Duration::ofMillis;
     * Function<String, String> boundRef = prefix::concat;
     * Function<String, String> unboundRef = String::trim;
     * Function<String, Exception> constructorRef = IOException::new;
     * ```
     *
     * ```kotlin
     * // Kotlin equivalents:
     * val staticRef: (Long) -> Duration = Duration::ofMillis
     * val boundRef: (String) -> String = prefix::plus
     * val unboundRef: (String) -> String = String::trim
     * val constructorRef: (String) -> IOException = ::IOException
     * ```
     */
    fun demonstrate() {
        println("=== Kotlin Function References (Java Method Reference equivalents) ===")

        // 1. Static method reference
        // Java: Function<Long, Duration> staticRef = Duration::ofMillis;
        val staticRef: (Long) -> Duration = Duration::ofMillis
        println("Static method ref: ${staticRef(1000L)}")

        // 2. Bound instance method reference
        // Java: Function<String, String> boundRef = prefix::concat;
        val prefix = "Result: "
        val boundRef: (String) -> String = prefix::plus
        println("Bound instance ref: ${boundRef("value")}")

        // 3. Unbound instance method reference
        // Java: Function<String, String> unboundRef = String::trim;
        val unboundRef: (String) -> String = String::trim
        println("Unbound instance ref: ${unboundRef("  hello  ")}")

        // 4. Constructor reference
        // Java: Function<String, IOException> constructorRef = IOException::new;
        val constructorRef: (String) -> IOException = ::IOException
        println("Constructor ref: ${constructorRef("test error").message}")

        // 5. Extension function reference (Kotlin-specific)
        val extRef: String.() -> String = String::trim
        println("Extension function ref: ${extRef("  world  ")}")

        // 6. Function composition using let/run
        val trim: (String) -> String = String::trim
        val lower: (String) -> String = String::lowercase
        val composed: (String) -> String = { s -> s.let(trim).let(lower) }
        println("Composed function: ${composed("  HELLO  ")}")
    }
}

/**
 * Main function demonstrating the complete retry functionality.
 */
fun main() {
    println("=== Kotlin Retry Executor Demo ===\n")

    // Demonstrate function references
    println("1. Function References (Java Method Reference equivalents):")
    MethodReferencesDemo.demonstrate()
    println()

    // Create a retry executor with various functional components
    println("2. Building RetryExecutor with function types:")

    val executor =
        RetryExecutor<String>(
            // Function type instead of @FunctionalInterface
            policy = RetryPolicies.exponentialBackoff(Duration.ofMillis(100), Duration.ofSeconds(2)),
            // Composing conditions with infix 'and' function
            condition =
                RetryConditions.maxAttempts(4) and
                    RetryConditions.forException<RuntimeException>(),
            // Lambda for logging (Consumer equivalent)
            onRetry = { ctx, delay ->
                println("  Retry attempt ${ctx.attempt}, waiting ${delay.toMillis()}ms")
            },
            // Lambda with method references for transformation
            transformer = { it.trim().lowercase() },
        )

    println("  Executor built successfully\n")

    // Execute with a flaky operation - trailing lambda syntax
    println("3. Executing flaky operation:")

    var counter = 0
    val result =
        executor.execute {
            counter++
            if (counter < 3) {
                throw RuntimeException("Simulated failure #$counter")
            }
            "  SUCCESS after $counter attempts  "
        }

    // Pattern matching using when expression
    println("\n4. Result (using when expression):")
    when (result) {
        is RetryResult.Success -> println("  Success: '${result.value}' in ${result.attempts} attempts")
        is RetryResult.Failure -> println("  Failure: ${result.error.message} after ${result.attempts} attempts")
    }

    // Demonstrate composition
    println("\n5. Policy composition:")
    val combinedPolicy =
        RetryPolicies.fixed(Duration.ofMillis(100))
            .maxWith(RetryPolicies.exponentialBackoff(Duration.ofMillis(50), Duration.ofSeconds(1)))
    println("  Combined policy delay for attempt 1: ${combinedPolicy(1, RuntimeException())}")
    println("  Combined policy delay for attempt 3: ${combinedPolicy(3, RuntimeException())}")

    // Demonstrate Kotlin-specific features
    println("\n6. Kotlin-specific features:")

    // Trailing lambda syntax
    println("  Trailing lambda: executor.execute { operation() }")

    // Infix functions for readable DSL
    val cond1 = RetryConditions.maxAttempts(3)
    val cond2 = RetryConditions.forException<IOException>()
    val combined = cond1 and cond2 // More readable than cond1.and(cond2)
    println("  Infix function: condition1 and condition2: ${combined(RetryContext.initial(IOException("IO error")))}")

    // Reified generics
    println("  Reified generic: forException<IOException>() - no Class parameter needed")

    // Result type with fold
    val kotlinResult =
        runCatching {
            "test".toInt()
        }
    val handled =
        kotlinResult.fold(
            onSuccess = { "Success: $it" },
            onFailure = { "Error: ${it.message}" },
        )
    println("  Result.fold: $handled")

    println("\n=== Demo Complete ===")
}
