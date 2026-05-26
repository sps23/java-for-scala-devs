package io.github.sps23.spring.mvc;

/**
 * Thrown when a trade with the requested ID does not exist.
 *
 * <p>
 * In a real Spring MVC application a {@code @ExceptionHandler} (or global
 * {@code @ControllerAdvice}) would catch this and return a 404 response:
 *
 * <pre>
 * {@code
 * @ExceptionHandler(TradeNotFoundException.class)
 * public ResponseEntity<ApiError> handleNotFound(TradeNotFoundException ex) {
 *     return ResponseEntity.status(HttpStatus.NOT_FOUND)
 *         .body(ApiError.of(404, ex.getMessage()));
 * }
 * }
 * </pre>
 */
public class TradeNotFoundException extends RuntimeException {

    public TradeNotFoundException(String tradeId) {
        super("Trade " + tradeId + " not found");
    }
}
