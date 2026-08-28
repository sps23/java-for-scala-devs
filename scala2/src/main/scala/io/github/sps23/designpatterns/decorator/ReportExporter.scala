package io.github.sps23.designpatterns.decorator

import java.nio.charset.StandardCharsets
import java.util.Base64

/** Decorator pattern in Scala 2.
  *
  * `ReportExporter` is the target trait every report-publishing client depends on. Concrete
  * decorators (compression, encryption, audit logging) can be stacked around a base exporter at
  * runtime, in any combination, without a new subclass per combination.
  */
trait ReportExporter {
  def exportReport(content: String): String
}

/** The base component: exports report content unchanged. */
class PlainTextReportExporter extends ReportExporter {
  override def exportReport(content: String): String = content
}

/** Base decorator holding the wrapped exporter so subclasses can delegate before/after adding
  * behavior.
  */
abstract class ReportExporterDecorator(protected val delegate: ReportExporter)
    extends ReportExporter

/** Simulates compressing the exported content by prefixing it with size metadata. */
class CompressionDecorator(delegate: ReportExporter) extends ReportExporterDecorator(delegate) {
  override def exportReport(content: String): String = {
    val exported = delegate.exportReport(content)
    s"COMPRESSED[${exported.length}]:$exported"
  }
}

/** Encrypts the exported content with a simple reversible XOR cipher encoded as Base64. Not
  * production-grade cryptography - it demonstrates a decorator that both transforms output and
  * exposes a way to reverse that transformation.
  */
class EncryptionDecorator(delegate: ReportExporter, key: Int)
    extends ReportExporterDecorator(delegate) {
  override def exportReport(content: String): String = {
    val exported = delegate.exportReport(content)
    EncryptionDecorator.PREFIX + Base64.getEncoder.encodeToString(
      EncryptionDecorator.xor(exported, key)
    )
  }
}

object EncryptionDecorator {
  private val PREFIX = "ENCRYPTED:"

  /** Reverses `exportReport` for a given key, useful for tests and audits. */
  def decrypt(encoded: String, key: Int): String = {
    val payload = if (encoded.startsWith(PREFIX)) encoded.substring(PREFIX.length) else encoded
    val decoded = Base64.getDecoder.decode(payload)
    new String(xor(new String(decoded, StandardCharsets.UTF_8), key), StandardCharsets.UTF_8)
  }

  private def xor(text: String, key: Int): Array[Byte] =
    text.toCharArray.map(c => (c ^ key).toByte)
}

/** Records an audit trail entry every time a report is exported, without changing the exported
  * content itself.
  */
class AuditLoggingDecorator(
    delegate: ReportExporter,
    auditLog: scala.collection.mutable.Buffer[String]
) extends ReportExporterDecorator(delegate) {
  override def exportReport(content: String): String = {
    val exported = delegate.exportReport(content)
    auditLog += s"Exported ${exported.length} characters"
    exported
  }
}
