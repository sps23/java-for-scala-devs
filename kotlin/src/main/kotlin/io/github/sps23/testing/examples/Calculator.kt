package io.github.sps23.testing.examples

import kotlin.math.sqrt

/**
 * A simple calculator for testing framework demonstrations.
 */
class Calculator {
    fun add(
        a: Int,
        b: Int,
    ): Int = a + b

    fun subtract(
        a: Int,
        b: Int,
    ): Int = a - b

    fun multiply(
        a: Int,
        b: Int,
    ): Int = a * b

    fun divide(
        a: Int,
        b: Int,
    ): Double {
        if (b == 0) {
            throw ArithmeticException("Division by zero")
        }
        return a.toDouble() / b
    }

    fun isPrime(number: Int): Boolean {
        if (number <= 1) return false
        for (i in 2..sqrt(number.toDouble()).toInt()) {
            if (number % i == 0) return false
        }
        return true
    }
}
