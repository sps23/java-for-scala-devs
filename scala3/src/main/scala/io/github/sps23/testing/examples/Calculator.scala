package io.github.sps23.testing.examples

/**
 * A simple calculator for testing framework demonstrations.
 */
class Calculator:
  def add(a: Int, b: Int): Int = a + b

  def subtract(a: Int, b: Int): Int = a - b

  def multiply(a: Int, b: Int): Int = a * b

  def divide(a: Int, b: Int): Double =
    if b == 0 then throw new ArithmeticException("Division by zero")
    else a.toDouble / b

  def isPrime(number: Int): Boolean =
    if number <= 1 then false
    else
      (2 to math.sqrt(number).toInt).forall(i => number % i != 0)
