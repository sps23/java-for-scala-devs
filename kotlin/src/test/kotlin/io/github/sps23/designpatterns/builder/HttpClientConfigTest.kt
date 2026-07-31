package io.github.sps23.designpatterns.builder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("HttpClientConfig Kotlin Builder Tests")
class HttpClientConfigTest {
    @Test
    @DisplayName("Should build config with defaults")
    fun shouldBuildWithDefaults() {
        val config = HttpClientConfigBuilder.builder("api.example.com", 443).build()

        assertEquals("api.example.com", config.host)
        assertEquals(443, config.port)
        assertEquals(500, config.connectTimeoutMs)
        assertEquals(2000, config.readTimeoutMs)
        assertTrue(config.useSsl)
        assertEquals(3, config.maxRetries)
        assertEquals(listOf(100, 200, 500), config.retryBackoffMs)
        assertEquals("application/json", config.defaultHeaders["Accept"])
        assertEquals("v1", config.apiVersion)
        assertTrue(config.enableCompression)
        assertEquals("https://api.example.com:443/api/v1", config.baseUrl)
    }

    @Test
    @DisplayName("Should build config with custom values")
    fun shouldBuildWithCustomValues() {
        val config =
            HttpClientConfigBuilder
                .builder("localhost", 8080)
                .connectTimeoutMs(300)
                .readTimeoutMs(1500)
                .useSsl(false)
                .maxRetries(2)
                .retryBackoffMs(listOf(50, 100))
                .defaultHeaders(mapOf("Accept" to "application/json"))
                .addDefaultHeader("X-Correlation-Id", "request-123")
                .apiVersion("v2")
                .circuitBreakerFailureThreshold(25)
                .enableCompression(false)
                .build()

        assertEquals(300, config.connectTimeoutMs)
        assertEquals(1500, config.readTimeoutMs)
        assertEquals(2, config.maxRetries)
        assertEquals(listOf(50, 100), config.retryBackoffMs)
        assertEquals("request-123", config.defaultHeaders["X-Correlation-Id"])
        assertEquals(25, config.circuitBreakerFailureThreshold)
        assertFalse(config.enableCompression)
        assertEquals("http://localhost:8080/api/v2", config.baseUrl)
    }

    @Test
    @DisplayName("Should reject timeout relationship violations")
    fun shouldRejectTimeoutRelationship() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                HttpClientConfigBuilder
                    .builder("api.example.com", 443)
                    .connectTimeoutMs(2000)
                    .readTimeoutMs(1000)
                    .build()
            }
        assertEquals("Read timeout must be greater than or equal to connect timeout", error.message)
    }

    @Test
    @DisplayName("Should reject retries outside supported range")
    fun shouldRejectRetriesRange() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                HttpClientConfigBuilder.builder("api.example.com", 443).maxRetries(11).build()
            }
        assertEquals("Max retries must be between 0 and 10", error.message)
    }

    @Test
    @DisplayName("Should reject retry backoff size mismatch")
    fun shouldRejectBackoffMismatch() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                HttpClientConfigBuilder
                    .builder("api.example.com", 443)
                    .maxRetries(2)
                    .retryBackoffMs(listOf(100))
                    .build()
            }
        assertEquals("Retry backoff size must match max retries", error.message)
    }

    @Test
    @DisplayName("Should reject blank header keys")
    fun shouldRejectBlankHeaderKey() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                HttpClientConfigBuilder
                    .builder("api.example.com", 443)
                    .addDefaultHeader(" ", "x")
                    .build()
            }
        assertEquals("Header key must not be blank", error.message)
    }

    @Test
    @DisplayName("Should reject invalid API versions")
    fun shouldRejectApiVersion() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                HttpClientConfigBuilder.builder("api.example.com", 443).apiVersion("latest").build()
            }
        assertEquals("API version must match pattern v{number}", error.message)
    }
}
