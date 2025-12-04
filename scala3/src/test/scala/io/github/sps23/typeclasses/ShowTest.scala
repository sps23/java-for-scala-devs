package io.github.sps23.typeclasses

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ShowTest extends AnyFunSuite with Matchers:

  import Show.*
  import Show.given

  test("Show[Int] should format integers correctly") {
    Show.showValue(42) shouldBe "Int(42)"
    Show.showValue(-5) shouldBe "Int(-5)"
  }

  test("Show[String] should format strings correctly") {
    Show.showValue("hello") shouldBe "\"hello\""
    Show.showValue("") shouldBe "\"\""
  }

  test("Show[Boolean] should format booleans correctly") {
    Show.showValue(true) shouldBe "True"
    Show.showValue(false) shouldBe "False"
  }

  test("Show[Double] should format doubles correctly") {
    Show.showValue(3.14) shouldBe "Double(3.14)"
  }

  test("Show[List[Int]] should format lists correctly") {
    Show.showValue(List(1, 2, 3)) shouldBe "List(Int(1), Int(2), Int(3))"
    Show.showValue(List.empty[Int]) shouldBe "List()"
  }

  test("Show[Option[Int]] should format options correctly") {
    Show.showValue(Some(42): Option[Int]) shouldBe "Some(Int(42))"
    Show.showValue(None: Option[Int]) shouldBe "None"
  }

  test("Show[Map[String, Int]] should format maps correctly") {
    val result = Show.showValue(Map("a" -> 1))
    result shouldBe "Map(\"a\" -> Int(1))"
  }

  test("Extension method .show should work") {
    42.show shouldBe "Int(42)"
    "hello".show shouldBe "\"hello\""
    List(1, 2, 3).show shouldBe "List(Int(1), Int(2), Int(3))"
  }

  test("Custom Show instance should work") {
    case class Person(name: String, age: Int)

    given Show[Person] with
      def show(p: Person): String = s"Person(${p.name}, ${p.age})"

    Show.showValue(Person("Alice", 30)) shouldBe "Person(Alice, 30)"
    Person("Bob", 35).show shouldBe "Person(Bob, 35)"
  }

  test("Nested collections should work") {
    val nested = List(List(1, 2), List(3, 4))
    nested.show shouldBe "List(List(Int(1), Int(2)), List(Int(3), Int(4)))"
  }

  test("Context bounds should work") {
    def printValue[A: Show](a: A): String = Show.showValue(a)

    printValue(42) shouldBe "Int(42)"
    printValue("test") shouldBe "\"test\""
  }

  test("Summoning instances should work") {
    val intShow = summon[Show[Int]]
    intShow.show(99) shouldBe "Int(99)"

    val stringShow = Show[String]
    stringShow.show("scala") shouldBe "\"scala\""
  }

  test("List of custom types should work with automatic derivation") {
    case class Person(name: String, age: Int)

    given Show[Person] with
      def show(p: Person): String = s"Person(${p.name}, ${p.age})"

    val people = List(Person("Alice", 30), Person("Bob", 35))
    people.show shouldBe "List(Person(Alice, 30), Person(Bob, 35))"
  }
