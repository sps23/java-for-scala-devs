package io.github.sps23.interview.preparation.async

/** Represents an API response with data from an external service.
  *
  * This case class demonstrates Scala's approach to immutable data in async contexts. Case classes
  * work seamlessly with Scala Futures for representing results from async operations.
  *
  * @param source
  *   the name of the API source
  * @param data
  *   the response data
  * @param timestamp
  *   when the response was received
  */
case class ApiResponse(source: String, data: String, timestamp: Long):
  require(source != null, "Source cannot be null")
  require(data != null, "Data cannot be null")
  require(timestamp > 0, "Timestamp must be positive")

object ApiResponse:
  /** Creates an ApiResponse with the current timestamp.
    *
    * @param source
    *   the API source name
    * @param data
    *   the response data
    * @return
    *   new ApiResponse instance
    */
  def apply(source: String, data: String): ApiResponse =
    ApiResponse(source, data, System.currentTimeMillis())
