package io.github.sps23.designpatterns.decorator

import java.util.Base64

/**
 * Decorator pattern in Kotlin.
 *
 * `ReportExporter` is the target interface every report-publishing client depends on. Concrete
 * decorators (compression, encryption, audit logging) can be stacked around a base exporter at
 * runtime, in any combination, without a new subclass per combination.
 */
sealed interface ReportExporter {
    fun exportReport(content: String): String
}

/** The base component: exports report content unchanged. */
class PlainTextReportExporter : ReportExporter {
    override fun exportReport(content: String): String = content
}

/**
 * Base decorator holding the wrapped exporter so subclasses can delegate before/after adding
 * behavior. Sealed classes are implicitly abstract, matching the pattern's intent that only the
 * concrete decorators below are instantiable.
 */
sealed class ReportExporterDecorator(
    protected val delegate: ReportExporter,
) : ReportExporter

/** Simulates compressing the exported content by prefixing it with size metadata. */
class CompressionDecorator(
    delegate: ReportExporter,
) : ReportExporterDecorator(delegate) {
    override fun exportReport(content: String): String {
        val exported = delegate.exportReport(content)
        return "COMPRESSED[${exported.length}]:$exported"
    }
}

/**
 * Encrypts the exported content with a simple reversible XOR cipher encoded as Base64. Not
 * production-grade cryptography - it demonstrates a decorator that both transforms output and
 * exposes a way to reverse that transformation.
 */
class EncryptionDecorator(
    delegate: ReportExporter,
    private val key: Int,
) : ReportExporterDecorator(delegate) {
    override fun exportReport(content: String): String {
        val exported = delegate.exportReport(content)
        return PREFIX + Base64.getEncoder().encodeToString(xor(exported, key))
    }

    companion object {
        private const val PREFIX = "ENCRYPTED:"

        /** Reverses [exportReport] for a given key, useful for tests and audits. */
        fun decrypt(
            encoded: String,
            key: Int,
        ): String {
            val payload = if (encoded.startsWith(PREFIX)) encoded.removePrefix(PREFIX) else encoded
            val decoded = Base64.getDecoder().decode(payload)
            return String(xor(String(decoded), key))
        }

        private fun xor(
            text: String,
            key: Int,
        ): ByteArray = ByteArray(text.length) { i -> (text[i].code xor key).toByte() }
    }
}

/**
 * Records an audit trail entry every time a report is exported, without changing the exported
 * content itself.
 */
class AuditLoggingDecorator(
    delegate: ReportExporter,
    private val auditLog: MutableList<String>,
) : ReportExporterDecorator(delegate) {
    override fun exportReport(content: String): String {
        val exported = delegate.exportReport(content)
        auditLog.add("Exported ${exported.length} characters")
        return exported
    }
}
