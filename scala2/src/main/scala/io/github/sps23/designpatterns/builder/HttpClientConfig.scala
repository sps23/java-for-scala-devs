package io.github.sps23.designpatterns.builder

final case class HttpClientConfig(
    host: String,
    port: Int,
    connectTimeoutMs: Int     = 500,
    readTimeoutMs: Int        = 2000,
    useSsl: Boolean           = true,
    maxRetries: Int           = 3,
    retryBackoffMs: List[Int] = List(100, 200, 500),
    defaultHeaders: Map[String, String] = Map(
      "Accept"     -> "application/json",
      "User-Agent" -> "java-for-scala-devs-client"
    ),
    circuitBreakerFailureThreshold: Int = 50,
    apiVersion: String                  = "v1",
    enableCompression: Boolean          = true
) {
  def baseUrl: String = {
    val scheme = if (useSsl) "https" else "http"
    s"$scheme://$host:$port/api/$apiVersion"
  }
}

object HttpClientConfigBuilder {
  def builder(host: String, port: Int): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(host = host, port = port)
}

final class HttpClientConfigBuilder private (
    host: String,
    port: Int,
    connectTimeoutMs: Int     = 500,
    readTimeoutMs: Int        = 2000,
    useSsl: Boolean           = true,
    maxRetries: Int           = 3,
    retryBackoffMs: List[Int] = List(100, 200, 500),
    defaultHeaders: Map[String, String] = Map(
      "Accept"     -> "application/json",
      "User-Agent" -> "java-for-scala-devs-client"
    ),
    circuitBreakerFailureThreshold: Int = 50,
    apiVersion: String                  = "v1",
    enableCompression: Boolean          = true
) {

  def connectTimeoutMs(value: Int): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      value,
      readTimeoutMs,
      useSsl,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )

  def readTimeoutMs(value: Int): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      value,
      useSsl,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )

  def useSsl(value: Boolean): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      value,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )

  def maxRetries(value: Int): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      value,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )

  def retryBackoffMs(values: List[Int]): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      maxRetries,
      values,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )

  def defaultHeaders(values: Map[String, String]): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      maxRetries,
      retryBackoffMs,
      values,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )

  def addDefaultHeader(key: String, value: String): HttpClientConfigBuilder =
    defaultHeaders(defaultHeaders + (key -> value))

  def circuitBreakerFailureThreshold(value: Int): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      value,
      apiVersion,
      enableCompression
    )

  def apiVersion(value: String): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      value,
      enableCompression
    )

  def enableCompression(value: Boolean): HttpClientConfigBuilder =
    new HttpClientConfigBuilder(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      value
    )

  def build(): HttpClientConfig = {
    validateHost(host)
    validatePort(port)
    validateTimeout(connectTimeoutMs, "Connect timeout")
    validateTimeout(readTimeoutMs, "Read timeout")
    validateTimeoutRelationship(connectTimeoutMs, readTimeoutMs)
    validateMaxRetries(maxRetries)
    validateBackoff(retryBackoffMs, maxRetries)
    validateHeaders(defaultHeaders)
    validateThreshold(circuitBreakerFailureThreshold)
    validateApiVersion(apiVersion)
    HttpClientConfig(
      host,
      port,
      connectTimeoutMs,
      readTimeoutMs,
      useSsl,
      maxRetries,
      retryBackoffMs,
      defaultHeaders,
      circuitBreakerFailureThreshold,
      apiVersion,
      enableCompression
    )
  }

  private def validateHost(value: String): Unit =
    if (value == null || value.trim.isEmpty)
      throw new IllegalArgumentException("Host must not be blank")

  private def validatePort(value: Int): Unit =
    if (value < 1 || value > 65535)
      throw new IllegalArgumentException("Port must be between 1 and 65535")

  private def validateTimeout(value: Int, name: String): Unit =
    if (value <= 0)
      throw new IllegalArgumentException(s"$name must be positive")

  private def validateTimeoutRelationship(connectValue: Int, readValue: Int): Unit =
    if (readValue < connectValue)
      throw new IllegalArgumentException(
        "Read timeout must be greater than or equal to connect timeout"
      )

  private def validateMaxRetries(value: Int): Unit =
    if (value < 0 || value > 10)
      throw new IllegalArgumentException("Max retries must be between 0 and 10")

  private def validateBackoff(values: List[Int], retries: Int): Unit = {
    if (values == null)
      throw new IllegalArgumentException("Retry backoff must not be null")
    if (values.size != retries)
      throw new IllegalArgumentException("Retry backoff size must match max retries")
    if (values.exists(_ <= 0))
      throw new IllegalArgumentException("Retry backoff values must be positive")
  }

  private def validateHeaders(values: Map[String, String]): Unit = {
    if (values == null)
      throw new IllegalArgumentException("Default headers must not be null")
    values.foreach {
      case (key, _) if key == null || key.trim.isEmpty =>
        throw new IllegalArgumentException("Header key must not be blank")
      case (_, headerValue) if headerValue == null || headerValue.trim.isEmpty =>
        throw new IllegalArgumentException("Header value must not be blank")
      case _ =>
    }
  }

  private def validateThreshold(value: Int): Unit =
    if (value < 1 || value > 100)
      throw new IllegalArgumentException("Circuit breaker threshold must be between 1 and 100")

  private def validateApiVersion(value: String): Unit =
    if (value == null || !value.matches("v\\d+"))
      throw new IllegalArgumentException("API version must match pattern v{number}")
}
