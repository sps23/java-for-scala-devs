package io.github.sps23.designpatterns.singleton

import scala.annotation.nowarn

/** Singleton using object in Scala 2.
  *
  * Like Kotlin, Scala provides the `object` keyword for singletons. The Scala runtime guarantees
  * thread-safe lazy initialization.
  *
  * This is the idiomatic way to implement singletons in Scala 2. Benefits:
  *   - ✓ Thread-safe by default
  *   - ✓ Lazy initialization
  *   - ✓ Minimal boilerplate
  *   - ✓ Can extend classes and implement traits
  */
object DatabaseConnection {
  println("DatabaseConnection instance created")

  def connect(): Unit = println("Connected to database")

  def disconnect(): Unit = println("Disconnected from database")
}

/** Alternative: Class-based singleton with companion object-like pattern.
  *
  * When you need instance-specific state, use a class with a companion singleton.
  */
class StatefulDatabaseConnection private () {
  private var isConnected: Boolean = false

  def connect(): Unit = {
    isConnected = true
    println("Connected to database (stateful)")
  }

  def disconnect(): Unit = {
    isConnected = false
    println("Disconnected from database (stateful)")
  }

  def isConnectedStatus: Boolean = isConnected
}

object StatefulDatabaseConnection {
  private val instance: StatefulDatabaseConnection = new StatefulDatabaseConnection()

  def getInstance: StatefulDatabaseConnection = instance
}
