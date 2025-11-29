package io.github.sps23.interview.preparation.async

/**
 * Represents aggregated data from multiple API sources.
 *
 * This data class holds the combined results from multiple async API calls,
 * including any partial failures that occurred during aggregation.
 *
 * @property responses successful API responses
 * @property errors any errors encountered during aggregation
 */
data class AggregatedData(
    val responses: List<ApiResponse>,
    val errors: List<String> = emptyList(),
) {
    // Note: Kotlin data class properties are non-nullable by default
    // The validation below is for defensive programming and documentation

    /**
     * Returns true if all API calls succeeded.
     *
     * @return true if no errors occurred
     */
    fun isFullySuccessful(): Boolean = errors.isEmpty()

    /**
     * Returns the count of successful responses.
     *
     * @return number of successful responses
     */
    fun successCount(): Int = responses.size

    companion object {
        /**
         * Creates an AggregatedData with only successful responses.
         *
         * @param responses the successful API responses
         * @return new AggregatedData instance
         */
        fun success(responses: List<ApiResponse>): AggregatedData = AggregatedData(responses, emptyList())

        /**
         * Creates an AggregatedData with responses and errors.
         *
         * @param responses the successful API responses
         * @param errors the error messages
         * @return new AggregatedData instance
         */
        fun partial(
            responses: List<ApiResponse>,
            errors: List<String>,
        ): AggregatedData = AggregatedData(responses, errors)
    }
}
