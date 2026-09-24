package io.github.sps23.interview.preparation.messagepassing

/** The "old way" of protecting shared mutable state: every caller must remember to synchronize on the same lock. Kept
  * around only as a contrast to the message-passing examples in [[MessagePassingExamples]], where a single owner thread
  * removes the need for locking entirely.
  */
final class LockedWallet:
  private val lock                = new Object()
  private var balanceInCents: Int = 0

  def topUp(cents: Int): Unit =
    lock.synchronized {
      // Every single caller - app, till, kiosk - must remember
      // to go through this method. Miss one spot and you have
      // a silent, hard-to-reproduce race condition.
      balanceInCents += cents
    }

  def balance: Int =
    lock.synchronized {
      balanceInCents
    }
