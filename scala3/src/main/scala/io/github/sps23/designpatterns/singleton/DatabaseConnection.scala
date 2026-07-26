package io.github.sps23.designpatterns.singleton

/** Singleton using object in Scala 3.
  *
  * Scala 3 continues the idiomatic `object` keyword pattern. This is even cleaner with Scala 3's
  * new syntax.
  *
  * Benefits:
  *   - ✓ Thread-safe by default
  *   - ✓ Lazy initialization
  *   - ✓ Minimal boilerplate
  *   - ✓ Can extend classes and implement traits
  */
object DatabaseConnection:
  println("DatabaseConnection instance created")

  def connect(): Unit = println("Connected to database")

  def disconnect(): Unit = println("Disconnected from database")

/** Alternative: Class-based singleton with companion object-like pattern.
  *
  * When you need instance-specific state, use a class with a singleton companion. Scala 3 makes
  * this even cleaner with its new syntax.
  */
class StatefulDatabaseConnection private ():
  private var isConnected: Boolean = false

  def connect(): Unit =
    isConnected = true
    println("Connected to database (stateful)")

  def disconnect(): Unit =
    isConnected = false
    println("Disconnected from database (stateful)")

  def isConnectedStatus: Boolean = isConnected

object StatefulDatabaseConnection:
  private val instance: StatefulDatabaseConnection = StatefulDatabaseConnection()

  def getInstance: StatefulDatabaseConnection = instance
