package io.github.sps23.interview.preparation.virtualthreads;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

/**
 * Scoped values example demonstrating Java 21's ScopedValue API (Preview).
 *
 * <p>
 * Scoped values are the modern replacement for ThreadLocal variables when using
 * virtual threads. Key differences:
 *
 * <table border="1">
 * <caption>ThreadLocal vs ScopedValue</caption>
 * <tr>
 * <th>Feature</th>
 * <th>ThreadLocal</th>
 * <th>ScopedValue</th>
 * </tr>
 * <tr>
 * <td>Mutability</td>
 * <td>Mutable</td>
 * <td>Immutable (per scope)</td>
 * </tr>
 * <tr>
 * <td>Inheritance</td>
 * <td>InheritableThreadLocal</td>
 * <td>Automatic in StructuredTaskScope</td>
 * </tr>
 * <tr>
 * <td>Memory</td>
 * <td>Stays until removed</td>
 * <td>Cleaned up with scope</td>
 * </tr>
 * <tr>
 * <td>Performance</td>
 * <td>Good for platform threads</td>
 * <td>Optimized for virtual threads</td>
 * </tr>
 * </table>
 *
 * <p>
 * For Scala developers: ScopedValue is similar to Cats Effect's IOLocal or
 * ZIO's FiberRef, providing context that flows through the call stack without
 * explicit parameter passing.
 *
 * <p>
 * Note: This is a preview feature in Java 21. Compile with --enable-preview
 * flag.
 */
public class ScopedValuesExample {

    /**
     * Scoped value for request ID - used for tracing/logging.
     *
     * <p>
     * Unlike ThreadLocal, ScopedValue is immutable within its scope. You set it
     * once when entering a scope, and it's available to all code within that scope
     * (including called methods).
     */
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    /**
     * Scoped value for user context - used for authorization.
     */
    private static final ScopedValue<UserContext> USER_CONTEXT = ScopedValue.newInstance();

    /**
     * User context record for authorization.
     */
    public record UserContext(String userId, String role, String tenantId) {

        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }

        public boolean canAccessTenant(String tenant) {
            return tenantId.equals(tenant) || isAdmin();
        }
    }

    private final HttpClient httpClient;

    public ScopedValuesExample() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Entry point that establishes scoped values for a request.
     *
     * <p>
     * The ScopedValue.where() method binds a value that is available to all code
     * called within the scope.
     *
     * @param userId
     *            the user making the request
     * @param role
     *            user's role for authorization
     * @param tenantId
     *            tenant context
     * @param url
     *            URL to fetch
     * @return response body
     */
    public String handleRequest(String userId, String role, String tenantId, String url)
            throws Exception {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        UserContext context = new UserContext(userId, role, tenantId);

        // Bind scoped values and run the request handler
        // Values are available to all code in this scope
        return ScopedValue.where(REQUEST_ID, requestId).where(USER_CONTEXT, context)
                .call(() -> processRequest(url));
    }

    /**
     * Processes a request - has access to scoped values without explicit
     * parameters.
     *
     * <p>
     * This method can access REQUEST_ID and USER_CONTEXT even though they weren't
     * passed as parameters. This is cleaner than passing context through every
     * method call.
     */
    private String processRequest(String url) throws Exception {
        // Access scoped values
        String requestId = REQUEST_ID.get();
        UserContext user = USER_CONTEXT.get();

        log("Processing request for user " + user.userId());

        // Authorization check using scoped user context
        if (!user.canAccessTenant(user.tenantId())) {
            throw new SecurityException("Access denied for tenant: " + user.tenantId());
        }

        // Make the actual HTTP call
        String response = fetchWithLogging(url);

        log("Request completed, response size: " + response.length());

        return response;
    }

    /**
     * Fetches a URL with logging that includes the request ID.
     *
     * <p>
     * Note how REQUEST_ID is available here without being passed as a parameter.
     * The scoped value flows through the call stack automatically.
     */
    private String fetchWithLogging(String url) throws Exception {
        log("Fetching: " + url);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .timeout(Duration.ofSeconds(30)).header("X-Request-ID", REQUEST_ID.get()) // Add
                                                                                          // request
                                                                                          // ID to
                                                                                          // outgoing
                                                                                          // request
                .GET().build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        log("Response status: " + response.statusCode());

        return response.body();
    }

    /**
     * Logging helper that includes request ID automatically.
     */
    private void log(String message) {
        String requestId = REQUEST_ID.isBound() ? REQUEST_ID.get() : "no-request";
        System.out.printf("[%s] %s%n", requestId, message);
    }

    /**
     * Demonstrates nesting scoped values - inner scope can override outer values.
     */
    public void demonstrateNesting() throws Exception {
        ScopedValue.where(REQUEST_ID, "outer-request").run(() -> {
            log("In outer scope");

            // Can create a nested scope with different values
            try {
                ScopedValue.where(REQUEST_ID, "inner-request").run(() -> {
                    log("In inner scope (different request ID)");
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            log("Back in outer scope (original request ID restored)");
        });
    }

    /**
     * Demonstrates checking if a scoped value is bound.
     */
    public void demonstrateBoundCheck() {
        // Outside any scope - value is not bound
        System.out.println("REQUEST_ID bound (outside scope): " + REQUEST_ID.isBound());

        ScopedValue.where(REQUEST_ID, "test-request").run(() -> {
            // Inside scope - value is bound
            System.out.println("REQUEST_ID bound (inside scope): " + REQUEST_ID.isBound());
            System.out.println("REQUEST_ID value: " + REQUEST_ID.get());
        });
    }

    /**
     * Example usage demonstrating scoped values.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Scoped Values Examples ===");
        System.out.println();

        ScopedValuesExample example = new ScopedValuesExample();

        // Example 1: Basic usage with HTTP request
        System.out.println("1. Basic scoped values with HTTP request:");
        try {
            String response = example.handleRequest("alice", "USER", "tenant-123",
                    "https://httpbin.org/get");
            System.out.println("   Response received: " + response.length() + " bytes");
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println();

        // Example 2: Nested scopes
        System.out.println("2. Nested scopes demonstration:");
        example.demonstrateNesting();

        System.out.println();

        // Example 3: Bound check
        System.out.println("3. Checking if scoped value is bound:");
        example.demonstrateBoundCheck();

        System.out.println();
        System.out.println("Benefits of ScopedValue over ThreadLocal:");
        System.out.println("- Immutable within scope (prevents accidental mutation)");
        System.out.println("- Automatic cleanup (no memory leaks)");
        System.out.println("- Optimized for virtual threads");
        System.out.println("- Clear scope boundaries (easier to reason about)");
    }
}
