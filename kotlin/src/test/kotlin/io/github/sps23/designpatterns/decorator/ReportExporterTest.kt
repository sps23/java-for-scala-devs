package io.github.sps23.designpatterns.decorator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decorator Pattern Kotlin Tests")
class ReportExporterTest {
    private val report = "Quarterly earnings: \$4.2M"

    @Test
    @DisplayName("Base exporter should return content unchanged")
    fun shouldExportPlainTextUnchanged() {
        val exporter: ReportExporter = PlainTextReportExporter()
        assertEquals(report, exporter.exportReport(report))
    }

    @Test
    @DisplayName("Compression decorator should wrap content with size metadata")
    fun shouldCompressExportedContent() {
        val exporter: ReportExporter = CompressionDecorator(PlainTextReportExporter())
        assertEquals("COMPRESSED[${report.length}]:$report", exporter.exportReport(report))
    }

    @Test
    @DisplayName("Encryption decorator should be reversible with the same key")
    fun shouldEncryptAndDecryptRoundTrip() {
        val exporter: ReportExporter = EncryptionDecorator(PlainTextReportExporter(), 42)
        val exported = exporter.exportReport(report)

        assertNotEquals(report, exported)
        assertTrue(exported.startsWith("ENCRYPTED:"))
        assertEquals(report, EncryptionDecorator.decrypt(exported, 42))
    }

    @Test
    @DisplayName("Audit logging decorator should record an entry without changing content")
    fun shouldRecordAuditLogWhenExporting() {
        val auditLog = mutableListOf<String>()
        val exporter: ReportExporter = AuditLoggingDecorator(PlainTextReportExporter(), auditLog)

        val exported = exporter.exportReport(report)

        assertEquals(report, exported)
        assertEquals(listOf("Exported ${report.length} characters"), auditLog)
    }

    @Test
    @DisplayName("Decorators should stack in any order without a subclass explosion")
    fun shouldStackMultipleDecoratorsInAnyOrder() {
        val auditLog = mutableListOf<String>()
        val exporter: ReportExporter =
            AuditLoggingDecorator(
                EncryptionDecorator(CompressionDecorator(PlainTextReportExporter()), 7),
                auditLog,
            )

        val exported = exporter.exportReport(report)

        assertTrue(exported.startsWith("ENCRYPTED:"))
        assertEquals(1, auditLog.size)
        assertEquals("Exported ${exported.length} characters", auditLog[0])

        val decrypted = EncryptionDecorator.decrypt(exported, 7)
        assertEquals("COMPRESSED[${report.length}]:$report", decrypted)
    }

    @Test
    @DisplayName("Real use case: publishing service builds a decorator chain from feature flags")
    fun shouldPublishSensitiveReportWithFullDecoratorChain() {
        val auditLog = mutableListOf<String>()
        val publishingService = ReportPublishingService(auditLog)

        val published =
            publishingService.publish(report, compress = true, encrypt = true, audit = true, encryptionKey = 99)

        assertTrue(published.startsWith("ENCRYPTED:"))
        assertEquals(1, auditLog.size)
    }
}

/**
 * Illustrates a realistic client: it composes decorators at runtime based on feature flags
 * instead of needing a dedicated exporter subclass for every combination.
 */
class ReportPublishingService(
    private val auditLog: MutableList<String>,
) {
    fun publish(
        content: String,
        compress: Boolean,
        encrypt: Boolean,
        audit: Boolean,
        encryptionKey: Int,
    ): String {
        var exporter: ReportExporter = PlainTextReportExporter()
        if (compress) exporter = CompressionDecorator(exporter)
        if (encrypt) exporter = EncryptionDecorator(exporter, encryptionKey)
        if (audit) exporter = AuditLoggingDecorator(exporter, auditLog)
        return exporter.exportReport(content)
    }
}
