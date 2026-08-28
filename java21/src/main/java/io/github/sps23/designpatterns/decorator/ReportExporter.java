package io.github.sps23.designpatterns.decorator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Decorator pattern in Java 21.
 *
 * <p>
 * {@code ReportExporter} is the target interface every report-publishing client
 * depends on. Concrete decorators (compression, encryption, audit logging) can
 * be stacked around a base exporter at runtime, in any combination, without
 * creating a new subclass for every combination.
 * </p>
 */
public sealed interface ReportExporter permits PlainTextReportExporter, ReportExporterDecorator {
    /**
     * Exports report content, returning the final representation after any wrapped
     * decorators have been applied.
     */
    String exportReport(String content);
}

/**
 * The base component: exports report content unchanged. Every decorator chain
 * ultimately wraps an instance of this class (or another concrete component
 * implementing the same interface).
 */
final class PlainTextReportExporter implements ReportExporter {
    @Override
    public String exportReport(String content) {
        return content;
    }
}

/**
 * Base decorator. Holds a reference to the wrapped {@link ReportExporter} so
 * concrete decorators can call through to it before or after adding their own
 * behavior.
 */
abstract sealed class ReportExporterDecorator implements ReportExporter
        permits CompressionDecorator, EncryptionDecorator, AuditLoggingDecorator {
    protected final ReportExporter delegate;

    protected ReportExporterDecorator(ReportExporter delegate) {
        this.delegate = delegate;
    }
}

/**
 * Simulates compressing the exported content. Real compression algorithms are
 * out of scope here; the goal is to demonstrate that a decorator can transform
 * the delegate's output before returning it to its own caller.
 */
final class CompressionDecorator extends ReportExporterDecorator {
    public CompressionDecorator(ReportExporter delegate) {
        super(delegate);
    }

    @Override
    public String exportReport(String content) {
        String exported = delegate.exportReport(content);
        return "COMPRESSED[" + exported.length() + "]:" + exported;
    }
}

/**
 * Encrypts the exported content with a simple reversible XOR cipher encoded as
 * Base64. This is intentionally not production-grade cryptography; it exists to
 * demonstrate a decorator that both transforms output and exposes a way to
 * reverse that transformation.
 */
final class EncryptionDecorator extends ReportExporterDecorator {
    private static final String PREFIX = "ENCRYPTED:";

    private final int key;

    public EncryptionDecorator(ReportExporter delegate, int key) {
        super(delegate);
        this.key = key;
    }

    @Override
    public String exportReport(String content) {
        String exported = delegate.exportReport(content);
        return PREFIX + Base64.getEncoder().encodeToString(xor(exported, key));
    }

    /**
     * Reverses {@link #exportReport(String)} for a given key, useful for tests and
     * audits.
     */
    public static String decrypt(String encoded, int key) {
        String payload = encoded.startsWith(PREFIX) ? encoded.substring(PREFIX.length()) : encoded;
        byte[] decoded = Base64.getDecoder().decode(payload);
        return new String(xor(new String(decoded, StandardCharsets.UTF_8), key),
                StandardCharsets.UTF_8);
    }

    private static byte[] xor(String text, int key) {
        byte[] bytes = new byte[text.length()];
        for (int i = 0; i < text.length(); i++) {
            bytes[i] = (byte) (text.charAt(i) ^ key);
        }
        return bytes;
    }
}

/**
 * Records an audit trail entry every time a report is exported, without
 * changing the exported content itself. This models a compliance requirement
 * that can be added or removed independently of compression or encryption.
 */
final class AuditLoggingDecorator extends ReportExporterDecorator {
    private final List<String> auditLog;

    public AuditLoggingDecorator(ReportExporter delegate, List<String> auditLog) {
        super(delegate);
        this.auditLog = auditLog;
    }

    @Override
    public String exportReport(String content) {
        String exported = delegate.exportReport(content);
        auditLog.add("Exported " + exported.length() + " characters");
        return exported;
    }
}
