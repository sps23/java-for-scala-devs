package io.github.sps23.designpatterns.singleton

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import scala.util.Using

@DisplayName("DatabaseConnection Scala 2 Singleton Tests")
class DatabaseConnectionTest {

  @Test
  @DisplayName("Should handle concurrent access safely")
  def testConcurrentAccess(): Unit = {
    val numThreads     = 100
    val startLatch     = new CountDownLatch(1)
    val endLatch       = new CountDownLatch(numThreads)
    val executionCount = new java.util.concurrent.atomic.AtomicInteger(0)

    // Scala 2.13+ provides Using for resource management (equivalent to Java try-with-resources):
    Using(Executors.newFixedThreadPool(10)) { executor =>
      for (_ <- 1 to numThreads)
        executor.submit(new Runnable {
          override def run(): Unit =
            try {
              startLatch.await()
              DatabaseConnection.connect()
              executionCount.incrementAndGet()
            } finally
              endLatch.countDown()
        })

      startLatch.countDown()
      endLatch.await()
    }

    assertEquals(numThreads, executionCount.get(), "All threads should execute without errors")
  }

  @Test
  @DisplayName("Should successfully connect to database")
  def testConnectionEstablished(): Unit = {
    val exec: org.junit.jupiter.api.function.Executable = () => DatabaseConnection.connect()
    assertDoesNotThrow(exec)
  }

  @Test
  @DisplayName("Should successfully disconnect from database")
  def testDisconnection(): Unit = {
    val exec: org.junit.jupiter.api.function.Executable = () => DatabaseConnection.disconnect()
    assertDoesNotThrow(exec)
  }

  @Test
  @DisplayName("Stateful companion object should be singleton")
  def testStatefulSingletonInstance(): Unit = {
    val conn1 = StatefulDatabaseConnection.getInstance
    val conn2 = StatefulDatabaseConnection.getInstance

    assertSame(conn1, conn2, "Instances should be identical")
  }

  @Test
  @DisplayName("Stateful singleton should maintain connection state")
  def testStatefulConnectionState(): Unit = {
    val conn                                            = StatefulDatabaseConnection.getInstance
    val exec: org.junit.jupiter.api.function.Executable = () => conn.connect()
    assertDoesNotThrow(exec)
    assertTrue(conn.isConnectedStatus, "Connection should be established")
  }
}
