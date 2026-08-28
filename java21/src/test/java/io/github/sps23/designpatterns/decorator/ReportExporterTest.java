package io.github.sps23.designpatterns.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Decorator Pattern Java 21 Tests")
class ReportExporterTest {

    private static final String REPORT = "Quarterly earnings: $4.2M";

    @Test
    @DisplayName("Base exporter should return content unchanged")
    void shouldExportPlainTextUnchanged() {
        ReportExporter exporter = new PlainTextReportExporter();

        assertEquals(REPORT, exporter.exportReport(REPORT));
    }

    @Test
    @DisplayName("Compression decorator should wrap content with size metadata")
    void shouldCompressExportedContent() {
        ReportExporter exporter = new CompressionDecorator(new PlainTextReportExporter());

        String exported = exporter.exportReport(REPORT);

        assertEquals("COMPRESSED[" + REPORT.length() + "]:" + REPORT, exported);
    }

    @Test
    @DisplayName("Encryption decorator should be reversible with the same key")
    void shouldEncryptAndDecryptRoundTrip() {
        ReportExporter exporter = new EncryptionDecorator(new PlainTextReportExporter(), 42);

        String exported = exporter.exportReport(REPORT);

        assertNotEquals(REPORT, exported);
        assertTrue(exported.startsWith("ENCRYPTED:"));
        assertEquals(REPORT, EncryptionDecorator.decrypt(exported, 42));
    }

    @Test
    @DisplayName("Audit logging decorator should record an entry without changing content")
    void shouldRecordAuditLogWhenExporting() {
        List<String> auditLog = new ArrayList<>();
        ReportExporter exporter = new AuditLoggingDecorator(new PlainTextReportExporter(),
                auditLog);

        String exported = exporter.exportReport(REPORT);

        assertEquals(REPORT, exported);
        assertEquals(List.of("Exported " + REPORT.length() + " characters"), auditLog);
    }

    @Test
    @DisplayName("Decorators should stack in any order without a subclass explosion")
    void shouldStackMultipleDecoratorsInAnyOrder() {
        List<String> auditLog = new ArrayList<>();
        ReportExporter exporter = new AuditLoggingDecorator(
                new EncryptionDecorator(new CompressionDecorator(new PlainTextReportExporter()), 7),
                auditLog);

        String exported = exporter.exportReport(REPORT);

        assertTrue(exported.startsWith("ENCRYPTED:"));
        assertEquals(1, auditLog.size());
        assertEquals("Exported " + exported.length() + " characters", auditLog.getFirst());

        String decrypted = EncryptionDecorator.decrypt(exported, 7);
        assertEquals("COMPRESSED[" + REPORT.length() + "]:" + REPORT, decrypted);
    }

    @Test
    @DisplayName("Real use case: publishing service builds a decorator chain from feature flags")
    void shouldPublishSensitiveReportWithFullDecoratorChain() {
        List<String> auditLog = new ArrayList<>();
        ReportPublishingService publishingService = new ReportPublishingService(auditLog);

        String published = publishingService.publish(REPORT, true, true, true, 99);

        assertTrue(published.startsWith("ENCRYPTED:"));
        assertEquals(1, auditLog.size());
    }
}

/**
 * Illustrates a realistic client: it composes decorators at runtime based on
 * feature flags (compliance requires an audit trail, sensitive reports require
 * encryption, large reports benefit from compression) instead of needing a
 * dedicated exporter subclass for every combination.
 */
final class ReportPublishingService {
    private final List<String> auditLog;

    ReportPublishingService(List<String> auditLog) {
        this.auditLog = auditLog;
    }

    String publish(String content, boolean compress, boolean encrypt, boolean audit,
            int encryptionKey) {
        ReportExporter exporter = new PlainTextReportExporter();
        if (compress) {
            exporter = new CompressionDecorator(exporter);
        }
        if (encrypt) {
            exporter = new EncryptionDecorator(exporter, encryptionKey);
        }
        if (audit) {
            exporter = new AuditLoggingDecorator(exporter, auditLog);
        }
        return exporter.exportReport(content);
    }
}
