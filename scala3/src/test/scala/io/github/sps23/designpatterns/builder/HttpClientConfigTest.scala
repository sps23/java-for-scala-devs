package io.github.sps23.designpatterns.builder

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class HttpClientConfigTest extends AnyFunSuite with Matchers:

  test("Builder should create config with defaults") {
    val config = HttpClientConfigBuilder.builder("api.example.com", 443).build()

    config.host shouldBe "api.example.com"
    config.port shouldBe 443
    config.connectTimeoutMs shouldBe 500
    config.readTimeoutMs shouldBe 2000
    config.useSsl shouldBe true
    config.maxRetries shouldBe 3
    config.retryBackoffMs shouldBe List(100, 200, 500)
    config.defaultHeaders("Accept") shouldBe "application/json"
    config.apiVersion shouldBe "v1"
    config.enableCompression shouldBe true
    config.baseUrl shouldBe "https://api.example.com:443/api/v1"
  }

  test("Builder should create config with custom values") {
    val config = HttpClientConfigBuilder
      .builder("localhost", 8080)
      .withConnectTimeoutMs(300)
      .withReadTimeoutMs(1500)
      .withUseSsl(false)
      .withMaxRetries(2)
      .withRetryBackoffMs(List(50, 100))
      .withDefaultHeaders(Map("Accept" -> "application/json"))
      .addDefaultHeader("X-Correlation-Id", "request-123")
      .withApiVersion("v2")
      .withCircuitBreakerFailureThreshold(25)
      .withEnableCompression(false)
      .build()

    config.connectTimeoutMs shouldBe 300
    config.readTimeoutMs shouldBe 1500
    config.maxRetries shouldBe 2
    config.retryBackoffMs shouldBe List(50, 100)
    config.defaultHeaders("X-Correlation-Id") shouldBe "request-123"
    config.circuitBreakerFailureThreshold shouldBe 25
    config.baseUrl shouldBe "http://localhost:8080/api/v2"
  }

  test("Builder should reject timeout relationship violations") {
    val error = the[IllegalArgumentException] thrownBy {
      HttpClientConfigBuilder
        .builder("api.example.com", 443)
        .withConnectTimeoutMs(2000)
        .withReadTimeoutMs(1000)
        .build()
    }
    error.getMessage shouldBe "Read timeout must be greater than or equal to connect timeout"
  }

  test("Builder should reject retries outside supported range") {
    val error = the[IllegalArgumentException] thrownBy {
      HttpClientConfigBuilder.builder("api.example.com", 443).withMaxRetries(11).build()
    }
    error.getMessage shouldBe "Max retries must be between 0 and 10"
  }

  test("Builder should reject retry backoff size mismatch") {
    val error = the[IllegalArgumentException] thrownBy {
      HttpClientConfigBuilder
        .builder("api.example.com", 443)
        .withMaxRetries(2)
        .withRetryBackoffMs(List(100))
        .build()
    }
    error.getMessage shouldBe "Retry backoff size must match max retries"
  }

  test("Builder should reject blank header keys") {
    val error = the[IllegalArgumentException] thrownBy {
      HttpClientConfigBuilder
        .builder("api.example.com", 443)
        .addDefaultHeader(" ", "x")
        .build()
    }
    error.getMessage shouldBe "Header key must not be blank"
  }

  test("Builder should reject invalid API versions") {
    val error = the[IllegalArgumentException] thrownBy {
      HttpClientConfigBuilder.builder("api.example.com", 443).withApiVersion("latest").build()
    }
    error.getMessage shouldBe "API version must match pattern v{number}"
  }
