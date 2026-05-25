package io.github.sps23.fibres

import zio.*

/** Typed error handling with ZIO fibres.
  *
  * ZIO's type signature is `ZIO[R, E, A]`:
  *   - `R` – the environment (dependencies) the effect needs
  *   - `E` – the typed error channel (what can go wrong)
  *   - `A` – the success value
  *
  * This is different from Java/Kotlin where errors are untyped exceptions. The compiler tells you
  * exactly what errors are possible, and you must handle all cases.
  *
  * Common type aliases: - `UIO[A]` = `ZIO[Any, Nothing, A]` – can never fail - `IO[E, A]` =
  * `ZIO[Any, E, A]` – can fail with `E` - `Task[A]` = `ZIO[Any, Throwable, A]` – can fail with any
  * exception
  *
  * For comparison with Java: - Java uses `CompletableFuture.exceptionally()` for error recovery,
  * but errors are untyped `Throwable`
  */
object FibreSupervision:

  /** A network error in our domain. */
  case class NetworkError(msg: String)

  /** A parse error in our domain. */
  case class ParseError(msg: String)

  /** A sealed union of all possible app errors. The compiler enforces exhaustive handling. */
  type AppError = NetworkError | ParseError

  /** An effect that always fails with a NetworkError. */
  val riskyFetch: IO[AppError, String] =
    ZIO.fail(NetworkError("timeout"))

  /** Recovers from any AppError and returns a fallback value.
    *
    * `catchAll` requires you to handle every variant of the error type. If you miss a case, it
    * won't compile.
    */
  val withFallback: UIO[String] =
    riskyFetch.catchAll:
      case NetworkError(msg) => ZIO.succeed(s"fallback (network: $msg)")
      case ParseError(msg)   => ZIO.succeed(s"fallback (parse: $msg)")

  /** Demonstrates interruption: a fibre can be cancelled at any point.
    *
    * When interrupted, ZIO ensures all finalizers (resource release) still run. This is the
    * "semantic interruption" model – you never leak resources, even under cancellation.
    *
    * `fibre.interrupt` returns `Exit[E, A]` – a value describing the outcome (success, failure, or
    * interruption). We inspect it to decide what to report.
    */
  val interruptionDemo: UIO[String] =
    for
      fibre <- ZIO.sleep(10.seconds).as("too slow").fork
      _     <- ZIO.sleep(50.millis)
      exit  <- fibre.interrupt
    yield if exit.isSuccess then "completed" else "interrupted"
