package io.github.sps23.interview.preparation.immutability

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CustomerProfileTest extends AnyFunSuite with Matchers:

  test("Should defensively copy mutable constructor inputs") {
    val roles       = scala.collection.mutable.ArrayBuffer("user")
    val preferences = scala.collection.mutable.Map("tier" -> "standard")

    val profile = CustomerProfile.create(1L, "alex@example.com", roles, preferences)
    roles += "admin"
    preferences += "region" -> "eu"

    profile.roles shouldBe List("user")
    profile.preferences shouldBe Map("tier" -> "standard")
  }

  test("Should create a new instance when adding a role") {
    val original = CustomerProfile.create(
      1L,
      "alex@example.com",
      Seq("user"),
      Map("tier" -> "standard")
    )

    val updated = original.withRole("admin")

    original.roles shouldBe List("user")
    updated.roles shouldBe List("user", "admin")
  }

  test("Should reject invalid email") {
    val error = the[IllegalArgumentException] thrownBy CustomerProfile.create(
      1L,
      "invalid-email",
      Seq("user"),
      Map("tier" -> "standard")
    )

    error.getMessage shouldBe "requirement failed: email must contain '@'"
  }
