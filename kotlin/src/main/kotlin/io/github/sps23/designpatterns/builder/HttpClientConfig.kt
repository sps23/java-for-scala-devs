package io.github.sps23.designpatterns.builder

data class HttpClientConfig(
    val host: String,
    val port: Int,
    val connectTimeoutMs: Int = 500,
    val readTimeoutMs: Int = 2000,
    val useSsl: Boolean = true,
    val maxRetries: Int = 3,
    val retryBackoffMs: List<Int> = listOf(100, 200, 500),
    val defaultHeaders: Map<String, String> =
        mapOf(
            "Accept" to "application/json",
            "User-Agent" to "java-for-scala-devs-client",
        ),
    val circuitBreakerFailureThreshold: Int = 50,
    val apiVersion: String = "v1",
    val enableCompression: Boolean = true,
) {
    val baseUrl: String
        get() {
            val scheme = if (useSsl) "https" else "http"
            return "$scheme://$host:$port/api/$apiVersion"
        }
}

class HttpClientConfigBuilder private constructor(
    private val host: String,
    private val port: Int,
) {
    private var connectTimeoutMs: Int = 500
    private var readTimeoutMs: Int = 2000
    private var useSsl: Boolean = true
    private var maxRetries: Int = 3
    private var retryBackoffMs: List<Int> = listOf(100, 200, 500)
    private val defaultHeaders: MutableMap<String, String> =
        linkedMapOf(
            "Accept" to "application/json",
            "User-Agent" to "java-for-scala-devs-client",
        )
    private var circuitBreakerFailureThreshold: Int = 50
    private var apiVersion: String = "v1"
    private var enableCompression: Boolean = true

    fun connectTimeoutMs(value: Int): HttpClientConfigBuilder {
        connectTimeoutMs = value
        return this
    }

    fun readTimeoutMs(value: Int): HttpClientConfigBuilder {
        readTimeoutMs = value
        return this
    }

    fun useSsl(value: Boolean): HttpClientConfigBuilder {
        useSsl = value
        return this
    }

    fun maxRetries(value: Int): HttpClientConfigBuilder {
        maxRetries = value
        return this
    }

    fun retryBackoffMs(values: List<Int>): HttpClientConfigBuilder {
        retryBackoffMs = values
        return this
    }

    fun defaultHeaders(values: Map<String, String>): HttpClientConfigBuilder {
        defaultHeaders.clear()
        defaultHeaders.putAll(values)
        return this
    }

    fun addDefaultHeader(
        key: String,
        value: String,
    ): HttpClientConfigBuilder {
        defaultHeaders[key] = value
        return this
    }

    fun circuitBreakerFailureThreshold(value: Int): HttpClientConfigBuilder {
        circuitBreakerFailureThreshold = value
        return this
    }

    fun apiVersion(value: String): HttpClientConfigBuilder {
        apiVersion = value
        return this
    }

    fun enableCompression(value: Boolean): HttpClientConfigBuilder {
        enableCompression = value
        return this
    }

    fun build(): HttpClientConfig {
        require(host.isNotBlank()) { "Host must not be blank" }
        require(port in 1..65535) { "Port must be between 1 and 65535" }
        require(connectTimeoutMs > 0) { "Connect timeout must be positive" }
        require(readTimeoutMs > 0) { "Read timeout must be positive" }
        require(readTimeoutMs >= connectTimeoutMs) {
            "Read timeout must be greater than or equal to connect timeout"
        }
        require(maxRetries in 0..10) { "Max retries must be between 0 and 10" }
        require(retryBackoffMs.size == maxRetries) { "Retry backoff size must match max retries" }
        require(retryBackoffMs.all { it > 0 }) { "Retry backoff values must be positive" }
        require(circuitBreakerFailureThreshold in 1..100) {
            "Circuit breaker threshold must be between 1 and 100"
        }
        require(Regex("v\\d+").matches(apiVersion)) {
            "API version must match pattern v{number}"
        }
        require(defaultHeaders.all { it.key.isNotBlank() }) { "Header key must not be blank" }
        require(defaultHeaders.all { it.value.isNotBlank() }) { "Header value must not be blank" }

        return HttpClientConfig(
            host = host,
            port = port,
            connectTimeoutMs = connectTimeoutMs,
            readTimeoutMs = readTimeoutMs,
            useSsl = useSsl,
            maxRetries = maxRetries,
            retryBackoffMs = retryBackoffMs.toList(),
            defaultHeaders = defaultHeaders.toMap(),
            circuitBreakerFailureThreshold = circuitBreakerFailureThreshold,
            apiVersion = apiVersion,
            enableCompression = enableCompression,
        )
    }

    companion object {
        fun builder(
            host: String,
            port: Int,
        ): HttpClientConfigBuilder = HttpClientConfigBuilder(host, port)
    }
}
