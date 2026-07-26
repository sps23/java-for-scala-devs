package io.github.sps23.designpatterns.singleton

/**
 * Singleton using object declaration in Kotlin.
 *
 * Kotlin's `object` keyword makes singleton implementation trivial and safe.
 * The runtime guarantees thread-safe lazy initialization of the singleton.
 *
 * This is the idiomatic way to implement singletons in Kotlin.
 * Benefits:
 * - ✓ Thread-safe by default
 * - ✓ Lazy initialization
 * - ✓ Minimal boilerplate
 * - ✓ Works with inheritance and interfaces
 */
object DatabaseConnection {
    init {
        println("DatabaseConnection instance created")
    }

    fun connect() {
        println("Connected to database")
    }

    fun disconnect() {
        println("Disconnected from database")
    }
}

/**
 * Alternative: Class-based singleton with companion object.
 *
 * Used when you need instance members that are truly instance-specific.
 * The companion object provides static-like access to the singleton.
 */
class StatefulDatabaseConnection private constructor() {
    private var isConnected = false

    fun connect() {
        isConnected = true
        println("Connected to database (stateful)")
    }

    fun disconnect() {
        isConnected = false
        println("Disconnected from database (stateful)")
    }

    fun isConnected(): Boolean = isConnected

    companion object {
        @Volatile
        private var instance: StatefulDatabaseConnection? = null

        fun getInstance(): StatefulDatabaseConnection =
            instance ?: synchronized(this) {
                instance ?: StatefulDatabaseConnection().also { instance = it }
            }
    }
}
