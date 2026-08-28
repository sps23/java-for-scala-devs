package io.github.sps23.designpatterns.decorator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import scala.collection.mutable.ArrayBuffer

@DisplayName("Decorator Pattern Scala 3 Tests")
class ReportExporterTest:

  private val report = "Quarterly earnings: $4.2M"

  @Test
  @DisplayName("Base exporter should return content unchanged")
  def shouldExportPlainTextUnchanged(): Unit =
    val exporter: ReportExporter = PlainTextReportExporter()
    assertEquals(report, exporter.exportReport(report))

  @Test
  @DisplayName("Compression decorator should wrap content with size metadata")
  def shouldCompressExportedContent(): Unit =
    val exporter: ReportExporter = CompressionDecorator(PlainTextReportExporter())
    assertEquals(s"COMPRESSED[${report.length}]:$report", exporter.exportReport(report))

  @Test
  @DisplayName("Encryption decorator should be reversible with the same key")
  def shouldEncryptAndDecryptRoundTrip(): Unit =
    val exporter: ReportExporter = EncryptionDecorator(PlainTextReportExporter(), 42)
    val exported                 = exporter.exportReport(report)

    assertNotEquals(report, exported)
    assertTrue(exported.startsWith("ENCRYPTED:"))
    assertEquals(report, EncryptionDecorator.decrypt(exported, 42))

  @Test
  @DisplayName("Audit logging decorator should record an entry without changing content")
  def shouldRecordAuditLogWhenExporting(): Unit =
    val auditLog                 = ArrayBuffer.empty[String]
    val exporter: ReportExporter = AuditLoggingDecorator(PlainTextReportExporter(), auditLog)

    val exported = exporter.exportReport(report)

    assertEquals(report, exported)
    assertEquals(1, auditLog.size)
    assertEquals(s"Exported ${report.length} characters", auditLog.head)

  @Test
  @DisplayName("Decorators should stack in any order without a subclass explosion")
  def shouldStackMultipleDecoratorsInAnyOrder(): Unit =
    val auditLog = ArrayBuffer.empty[String]
    val exporter: ReportExporter = AuditLoggingDecorator(
      EncryptionDecorator(CompressionDecorator(PlainTextReportExporter()), 7),
      auditLog
    )

    val exported = exporter.exportReport(report)

    assertTrue(exported.startsWith("ENCRYPTED:"))
    assertEquals(1, auditLog.size)
    assertEquals(s"Exported ${exported.length} characters", auditLog.head)

    val decrypted = EncryptionDecorator.decrypt(exported, 7)
    assertEquals(s"COMPRESSED[${report.length}]:$report", decrypted)

  @Test
  @DisplayName("Real use case: publishing service builds a decorator chain from feature flags")
  def shouldPublishSensitiveReportWithFullDecoratorChain(): Unit =
    val auditLog          = ArrayBuffer.empty[String]
    val publishingService = ReportPublishingService(auditLog)

    val published =
      publishingService.publish(
        report,
        compress      = true,
        encrypt       = true,
        audit         = true,
        encryptionKey = 99
      )

    assertTrue(published.startsWith("ENCRYPTED:"))
    assertEquals(1, auditLog.size)

/** Illustrates a realistic client: it composes decorators at runtime based on feature flags instead
  * of needing a dedicated exporter subclass for every combination.
  */
class ReportPublishingService(auditLog: ArrayBuffer[String]):
  def publish(
      content: String,
      compress: Boolean,
      encrypt: Boolean,
      audit: Boolean,
      encryptionKey: Int
  ): String =
    var exporter: ReportExporter = PlainTextReportExporter()
    if compress then exporter = CompressionDecorator(exporter)
    if encrypt then exporter  = EncryptionDecorator(exporter, encryptionKey)
    if audit then exporter    = AuditLoggingDecorator(exporter, auditLog)
    exporter.exportReport(content)
