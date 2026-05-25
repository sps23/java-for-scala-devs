package io.github.sps23.fibres;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Structured concurrency patterns with {@link StructuredTaskScope} – the Java
 * equivalent of ZIO's concurrent combinators.
 *
 * <p>
 * {@link StructuredTaskScope} is a preview feature in Java 21 (compile with
 * {@code --enable-preview}). It guarantees:
 * <ul>
 * <li>No orphaned threads – tasks can't outlive their scope</li>
 * <li>Automatic cancellation – the scope cancels remaining tasks when
 * appropriate</li>
 * <li>Clean error propagation – exceptions surface to the caller</li>
 * </ul>
 *
 * <p>
 * Two built-in strategies:
 * <ul>
 * <li>{@link StructuredTaskScope.ShutdownOnSuccess} – race: return the first
 * success (like ZIO's {@code race})</li>
 * <li>{@link StructuredTaskScope.ShutdownOnFailure} – all must succeed (like
 * ZIO's {@code collectAllPar})</li>
 * </ul>
 */
@SuppressWarnings("preview")
public class StructuredTaskScopeDemo {

    /**
     * Races a "cache" source against a "database" source and returns whichever
     * responds first.
     *
     * <p>
     * Equivalent to ZIO's {@code fromCache race fromDb} and Kotlin's {@code select
     * { async { }.onAwait { } }}.
     *
     * @return the result from the fastest data source
     * @throws Exception
     *             if no source succeeds
     */
    public static String raceDataSources() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            scope.fork(() -> {
                Thread.sleep(10);
                return "cached";
            });
            scope.fork(() -> {
                Thread.sleep(200);
                return "db";
            });
            scope.join();
            return scope.result();
        }
    }

    /**
     * Fetches all URLs in parallel and collects every result.
     *
     * <p>
     * Equivalent to ZIO's {@code ZIO.collectAllPar} and Kotlin's {@code urls.map {
     * async { } }.awaitAll()}.
     *
     * @param urls
     *            the list of URLs to fetch concurrently
     * @return a list of content strings, one per URL
     * @throws Exception
     *             if any fetch fails
     */
    public static List<String> fetchAllUrls(List<String> urls) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<Subtask<String>> subtasks = urls.stream()
                    .map(url -> scope.fork(() -> "content of " + url)).toList();
            scope.join().throwIfFailed();
            return subtasks.stream().map(Subtask::get).toList();
        }
    }
}
