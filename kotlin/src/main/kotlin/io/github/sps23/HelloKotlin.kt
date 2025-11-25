package io.github.sps23

/**
 * Example Kotlin class demonstrating basic functionality.
 */
fun main() {
    println(HelloKotlin.greeting())
}

object HelloKotlin {
    fun greeting(): String = "Hello from Kotlin!"

    // Example of Kotlin specific features
    fun processItems(items: List<String>): List<String> =
        items.map { it.uppercase() }.filter { it.isNotEmpty() }

    // Data class example
    data class Person(val name: String, val age: Int)

    // Extension function
    fun String.exclaim(): String = "$this!"
}
