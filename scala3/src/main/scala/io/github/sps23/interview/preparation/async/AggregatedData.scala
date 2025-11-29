package io.github.sps23.interview.preparation.async

/** Represents aggregated data from multiple API sources.
  *
  * This case class holds the combined results from multiple async API calls, including any partial
  * failures that occurred during aggregation.
  *
  * @param responses
  *   successful API responses
  * @param errors
  *   any errors encountered during aggregation
  */
case class AggregatedData(responses: List[ApiResponse], errors: List[String]):
  require(responses != null, "Responses cannot be null")
  require(errors != null, "Errors cannot be null")

  /** Returns true if all API calls succeeded.
    *
    * @return
    *   true if no errors occurred
    */
  def isFullySuccessful: Boolean = errors.isEmpty

  /** Returns the count of successful responses.
    *
    * @return
    *   number of successful responses
    */
  def successCount: Int = responses.size

object AggregatedData:
  /** Creates an AggregatedData with only successful responses.
    *
    * @param responses
    *   the successful API responses
    * @return
    *   new AggregatedData instance
    */
  def success(responses: List[ApiResponse]): AggregatedData =
    AggregatedData(responses, List.empty)

  /** Creates an AggregatedData with responses and errors.
    *
    * @param responses
    *   the successful API responses
    * @param errors
    *   the error messages
    * @return
    *   new AggregatedData instance
    */
  def partial(responses: List[ApiResponse], errors: List[String]): AggregatedData =
    AggregatedData(responses, errors)
