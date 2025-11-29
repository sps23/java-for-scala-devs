package io.github.sps23.interview.preparation.retry

import java.time.{Duration, Instant}
import java.io.IOException
import scala.util.Try
import scala.util.control.NonFatal

/** Retry context holding information about the current retry state.
  *
  * @param attempt
  *   current attempt number (1-based)
  * @param startTime
  *   when the first attempt started
  * @param lastError
  *   exception from the most recent failed attempt
  */
case class RetryContext(
    attempt: Int,
    startTime: Instant,
    lastError: Throwable
):
  /** Creates a new context for the next retry attempt. */
  def nextAttempt(newError: Throwable): RetryContext =
    copy(attempt = attempt + 1, lastError = newError)

  /** Gets the elapsed time since the first attempt. */
  def elapsed: Duration = Duration.between(startTime, Instant.now())

object RetryContext:
  /** Creates a new context for the first failed attempt. */
  def initial(error: Throwable): RetryContext =
    RetryContext(1, Instant.now(), error)

/** Represents the result of a retry operation.
  *
  * Comparison with Java:
  * {{{
  * // Java uses sealed interface with record implementations
  * sealed interface RetryResult<T> {
  *     record Success<T>(T value, int attempts) implements RetryResult<T> {}
  *     record Failure<T>(Throwable error, int attempts) implements RetryResult<T> {}
  * }
  * }}}
  */
enum RetryResult[+T]:
  case Success(value: T, attempts: Int)
  case Failure(error: Throwable, attempts: Int)

  def isSuccess: Boolean = this match
    case Success(_, _) => true
    case Failure(_, _) => false

  def attemptCount: Int = this match
    case Success(_, a) => a
    case Failure(_, a) => a

/** Custom exception for retry-related failures. */
class RetryException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)

/** A configurable retry executor demonstrating functional programming in Scala 3.
  *
  * This class showcases key Scala functional programming concepts and compares them with Java:
  *
  *   - Function types vs Java's @FunctionalInterface
  *   - Function composition with andThen and compose
  *   - Partial functions for exception handling
  *   - Higher-order functions
  *   - Extension methods (Scala 3)
  *
  * Comparison with Java:
  * {{{
  * // Java requires explicit functional interfaces:
  * @FunctionalInterface
  * interface RetryPolicy {
  *     Duration delayFor(int attempt, Throwable error);
  * }
  *
  * // Scala uses simple function types:
  * type RetryPolicy = (Int, Throwable) => Duration
  * }}}
  *
  * Example usage:
  * {{{
  * val executor = RetryExecutor[String](
  *   policy = RetryPolicy.exponentialBackoff(100.millis, 5.seconds),
  *   condition = RetryCondition.maxAttempts(3) && RetryCondition.forExceptions[IOException],
  *   onRetry = (ctx, delay) => println(s"Retrying in $delay"),
  *   transformer = _.trim.toLowerCase
  * )
  *
  * val result = executor.execute(() => fetchData())
  * }}}
  *
  * @param policy
  *   function determining delay between retries
  * @param condition
  *   function determining whether to retry
  * @param onRetry
  *   callback invoked before each retry (Consumer equivalent)
  * @param transformer
  *   function to transform successful results
  */
class RetryExecutor[T](
    policy: RetryPolicy       = RetryPolicy.fixed(Duration.ofSeconds(1)),
    condition: RetryCondition = RetryCondition.maxAttempts(3),
    onRetry: RetryListener = (_, _) => (),
    transformer: T => T = identity[T]
):

  /** Executes an operation with retry logic.
    *
    * @param operation
    *   the operation to execute (by-name parameter acts like Supplier)
    * @return
    *   the retry result
    */
  def execute(operation: => T): RetryResult[T] =
    var attempt               = 0
    var context: RetryContext = null

    while true do
      attempt += 1
      Try(operation) match
        case scala.util.Success(result) =>
          // Apply transformation - equivalent to Java's Function.apply()
          val transformed = transformer(result)
          return RetryResult.Success(transformed, attempt)

        case scala.util.Failure(e) =>
          context =
            if context == null then RetryContext.initial(e)
            else context.nextAttempt(e)

          // Check retry condition - equivalent to Java's Predicate.test()
          if !condition(context) then return RetryResult.Failure(e, attempt)

          // Calculate delay - equivalent to Java's custom functional interface
          val delay = policy(attempt, e)

          // Notify listener - equivalent to Java's Consumer.accept()
          onRetry(context, delay)

          // Wait before retry
          sleep(delay)

    // Unreachable, but needed for compilation
    RetryResult.Failure(new RetryException("Unexpected exit"), attempt)

  private def sleep(delay: Duration): Unit =
    try Thread.sleep(delay.toMillis)
    catch
      case ie: InterruptedException =>
        Thread.currentThread().interrupt()
        throw RetryException("Retry interrupted", ie)

/** Type aliases demonstrating Scala's simpler approach to functional interfaces.
  *
  * Comparison with Java:
  * {{{
  * // Java requires @FunctionalInterface annotation:
  * @FunctionalInterface
  * public interface RetryPolicy {
  *     Duration delayFor(int attempt, Throwable error);
  * }
  *
  * // Scala uses simple type aliases:
  * type RetryPolicy = (Int, Throwable) => Duration
  * }}}
  */
object TypeAliases:
  /** Equivalent to Java's RetryPolicy functional interface. */
  type RetryPolicy = (Int, Throwable) => Duration

  /** Equivalent to Java's RetryCondition functional interface. */
  type ShouldRetry = RetryContext => Boolean

  /** Equivalent to Java's Consumer<RetryEvent>. */
  type RetryListener = (RetryContext, Duration) => Unit

/** Retry policy functions - equivalent to Java's RetryPolicy interface. */
type RetryPolicy = (Int, Throwable) => Duration

object RetryPolicy:
  /** Creates a fixed delay policy.
    *
    * Comparison with Java:
    * {{{
    * // Java
    * static RetryPolicy fixed(Duration delay) {
    *     return (attempt, error) -> delay;
    * }
    * }}}
    */
  def fixed(delay: Duration): RetryPolicy =
    (_, _) => delay

  /** Creates an exponential backoff policy.
    *
    * Demonstrates lambda with calculation logic, same as Java.
    */
  def exponentialBackoff(initialDelay: Duration, maxDelay: Duration): RetryPolicy =
    (attempt, _) =>
      val delayMs = initialDelay.toMillis * Math.pow(2, attempt - 1).toLong
      Duration.ofMillis(Math.min(delayMs, maxDelay.toMillis))

  /** Creates a policy that varies delay based on exception type. */
  def errorSpecific(networkDelay: Duration, otherDelay: Duration): RetryPolicy =
    (_, error) =>
      error match
        case _: IOException => networkDelay
        case _              => otherDelay

  /** Extension methods for RetryPolicy composition.
    *
    * Comparison with Java:
    * {{{
    * // Java uses default methods in the interface
    * default RetryPolicy maxWith(RetryPolicy other) { ... }
    *
    * // Scala uses extension methods
    * extension (self: RetryPolicy)
    *   def maxWith(other: RetryPolicy): RetryPolicy = ...
    * }}}
    */
  extension (self: RetryPolicy)
    def maxWith(other: RetryPolicy): RetryPolicy =
      (attempt, error) =>
        val selfDelay  = self(attempt, error)
        val otherDelay = other(attempt, error)
        if selfDelay.compareTo(otherDelay) > 0 then selfDelay else otherDelay

/** Retry condition functions - equivalent to Java's RetryCondition interface. */
type RetryCondition = RetryContext => Boolean

object RetryCondition:
  /** Creates a condition that limits retry attempts. */
  def maxAttempts(max: Int): RetryCondition =
    ctx => ctx.attempt < max

  /** Creates a condition based on exception type.
    *
    * Comparison with Java:
    * {{{
    * // Java uses Class.isInstance and varargs
    * static RetryCondition forExceptions(Class<? extends Throwable>... types) {
    *     return ctx -> Arrays.stream(types).anyMatch(t -> t.isInstance(ctx.lastError()));
    * }
    *
    * // Scala uses ClassTag for type checking
    * def forExceptions[E <: Throwable: ClassTag]: RetryCondition = ...
    * }}}
    */
  def forExceptions[E <: Throwable: reflect.ClassTag]: RetryCondition =
    ctx =>
      val errorClass = reflect.classTag[E].runtimeClass
      errorClass.isInstance(ctx.lastError)

  /** Creates a condition from multiple exception types. */
  def forExceptionTypes(types: Class[? <: Throwable]*): RetryCondition =
    ctx => types.exists(_.isInstance(ctx.lastError))

  /** Creates a condition that checks exception message. */
  def messageContains(pattern: String): RetryCondition =
    ctx => Option(ctx.lastError.getMessage).exists(_.contains(pattern))

  /** Extension methods for RetryCondition composition.
    *
    * Comparison with Java:
    * {{{
    * // Java uses default methods
    * default RetryCondition and(RetryCondition other) {
    *     return ctx -> this.shouldRetry(ctx) && other.shouldRetry(ctx);
    * }
    *
    * // Scala uses extension methods with symbolic names
    * extension (self: RetryCondition)
    *   def &&(other: RetryCondition): RetryCondition = ...
    * }}}
    */
  extension (self: RetryCondition)
    def &&(other: RetryCondition): RetryCondition =
      ctx => self(ctx) && other(ctx)

    def ||(other: RetryCondition): RetryCondition =
      ctx => self(ctx) || other(ctx)

    def unary_! : RetryCondition =
      ctx => !self(ctx)

/** Retry listener type - equivalent to Java's Consumer<RetryEvent>. */
type RetryListener = (RetryContext, Duration) => Unit

/** Demonstrates method references comparison between Scala and Java. */
object MethodReferencesDemo:
  import java.time.Duration

  /** Demonstrates various function references and their Java equivalents.
    *
    * Scala uses first-class functions while Java uses method references:
    *
    * {{{
    * // Java method references:
    * Function<Long, Duration> staticRef = Duration::ofMillis;           // Static
    * Function<String, String> boundRef = prefix::concat;                // Bound instance
    * Function<String, String> unboundRef = String::trim;                // Unbound instance
    * Function<String, Exception> constructorRef = IOException::new;     // Constructor
    *
    * // Scala equivalents:
    * val staticRef: Long => Duration = Duration.ofMillis               // Static method
    * val boundRef: String => String = prefix.concat                    // Bound instance
    * val unboundRef: String => String = _.trim                         // Lambda (more common)
    * val constructorRef: String => Exception = new IOException(_)      // Constructor
    * }}}
    */
  def demonstrate(): Unit =
    println("=== Scala Function References (Java Method Reference equivalents) ===")

    // 1. Static method reference equivalent
    // Java: Function<Long, Duration> staticRef = Duration::ofMillis;
    val staticRef: Long => Duration = Duration.ofMillis
    println(s"Static method ref: ${staticRef(1000L)}")

    // 2. Bound instance method reference equivalent
    // Java: Function<String, String> boundRef = prefix::concat;
    val prefix = "Result: "
    val boundRef: String => String = prefix.concat
    println(s"Bound instance ref: ${boundRef("value")}")

    // 3. Unbound instance method reference equivalent
    // Java: Function<String, String> unboundRef = String::trim;
    // Scala typically uses underscore syntax instead
    val unboundRef: String => String = _.trim
    println(s"Unbound instance ref (lambda): ${unboundRef("  hello  ")}")

    // 4. Constructor reference equivalent
    // Java: Function<String, IOException> constructorRef = IOException::new;
    val constructorRef: String => IOException = new IOException(_)
    println(s"Constructor ref: ${constructorRef("test error").getMessage}")

    // 5. Partial function application (not available in Java)
    def add(a: Int, b: Int): Int = a + b
    val addFive: Int => Int = add(5, _)
    println(s"Partial application: ${addFive(3)}")

    // 6. Function composition (more elegant than Java's andThen)
    val trim: String     => String = _.trim
    val lower: String    => String = _.toLowerCase
    val composed: String => String = trim andThen lower
    println(s"Composed function: ${composed("  HELLO  ")}")

/** Main object demonstrating the complete retry functionality. */
@main def runRetryDemo(): Unit =
  import RetryPolicy.*
  import RetryCondition.*

  println("=== Scala 3 Retry Executor Demo ===\n")

  // Demonstrate function references
  println("1. Function References (Java Method Reference equivalents):")
  MethodReferencesDemo.demonstrate()
  println()

  // Create a retry executor with various functional components
  println("2. Building RetryExecutor with function types:")

  val executor = RetryExecutor[String](
    // Function type instead of @FunctionalInterface
    policy = RetryPolicy.exponentialBackoff(Duration.ofMillis(100), Duration.ofSeconds(2)),
    // Composing conditions with && extension method
    condition = RetryCondition.maxAttempts(4) && RetryCondition.forExceptions[RuntimeException],
    // Function literal for logging (Consumer equivalent)
    onRetry =
      (ctx, delay) => println(s"  Retry attempt ${ctx.attempt}, waiting ${delay.toMillis}ms"),
    // Function composition with andThen
    transformer = (s: String) => s.trim.toLowerCase
  )

  println("  Executor built successfully\n")

  // Execute with a flaky operation
  println("3. Executing flaky operation:")

  var counter = 0
  val result = executor.execute {
    counter += 1
    if counter < 3 then throw RuntimeException(s"Simulated failure #$counter")
    s"  SUCCESS after $counter attempts  "
  }

  // Pattern matching on result (more elegant than Java's switch)
  println("\n4. Result (using pattern matching):")
  result match
    case RetryResult.Success(value, attempts) =>
      println(s"  Success: '$value' in $attempts attempts")
    case RetryResult.Failure(error, attempts) =>
      println(s"  Failure: ${error.getMessage} after $attempts attempts")

  // Demonstrate composition
  println("\n5. Policy composition:")
  val combinedPolicy =
    RetryPolicy
      .fixed(Duration.ofMillis(100))
      .maxWith(RetryPolicy.exponentialBackoff(Duration.ofMillis(50), Duration.ofSeconds(1)))
  println(s"  Combined policy delay for attempt 1: ${combinedPolicy(1, RuntimeException())}")
  println(s"  Combined policy delay for attempt 3: ${combinedPolicy(3, RuntimeException())}")

  // Demonstrate Scala-specific features
  println("\n6. Scala-specific features:")

  // Using by-name parameters (no explicit Supplier needed)
  println("  By-name parameter: No Supplier wrapper needed")

  // Using partial function for error handling
  val handleSpecificErrors: PartialFunction[Throwable, String] =
    case e: IOException      => s"IO Error: ${e.getMessage}"
    case e: RuntimeException => s"Runtime Error: ${e.getMessage}"

  Try("test".toInt).recover(handleSpecificErrors) match
    case scala.util.Success(v) => println(s"  Result: $v")
    case scala.util.Failure(e) =>
      println(s"  Handled: ${handleSpecificErrors.lift(e).getOrElse("Unknown")}")

  println("\n=== Demo Complete ===")
