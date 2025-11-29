package io.github.sps23.interview.preparation.async

/**
 * Represents an API response with data from an external service.
 *
 * This data class demonstrates Kotlin's approach to immutable data in async contexts.
 * Data classes work seamlessly with Kotlin Coroutines for representing results from
 * async operations.
 *
 * @property source the name of the API source
 * @property data the response data
 * @property timestamp when the response was received
 */
data class ApiResponse(
    val source: String,
    val data: String,
    val timestamp: Long = System.currentTimeMillis(),
) {
    init {
        require(source.isNotBlank()) { "Source cannot be blank" }
        require(data.isNotBlank()) { "Data cannot be blank" }
        require(timestamp > 0) { "Timestamp must be positive" }
    }

    companion object {
        /**
         * Creates an ApiResponse with the current timestamp.
         *
         * @param source the API source name
         * @param data the response data
         * @return new ApiResponse instance
         */
        fun of(
            source: String,
            data: String,
        ): ApiResponse = ApiResponse(source, data)
    }
}
