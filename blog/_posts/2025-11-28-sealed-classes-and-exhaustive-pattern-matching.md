---
layout: post
title: "Sealed Classes and Exhaustive Pattern Matching"
date: 2025-11-28 15:00:00 +0000
categories: interview
tags: java java21 scala kotlin sealed-classes pattern-matching interview-preparation
---

Sealed classes are a powerful feature for type-safe domain modeling. Java 17 introduced sealed classes and interfaces, bringing Java closer to Scala's sealed traits and Kotlin's sealed classes. In this post, we'll explore how to model a payment system using sealed classes and implement fee calculation with exhaustive pattern matching.

## The Problem: Type-Safe Payment Processing

Imagine we need to model a payment system that supports three payment methods:
- **Credit Card** - 2.9% + $0.30 per transaction
- **Bank Transfer** - flat $2.50 (under $1000) or $5.00 (over $1000)
- **Digital Wallet** - 2.5% with $0.50 minimum

We want the compiler to:
1. Restrict which types can represent a payment method
2. Ensure we handle all payment types in our fee calculation
3. Enable destructuring of payment data in pattern matching

## Key Concepts

### Sealed Classes/Interfaces

In all three languages, sealing a type restricts which other types can extend or implement it:

| Language | Keyword | Behavior |
|----------|---------|----------|
| Java | `sealed ... permits` | Must explicitly list permitted subtypes |
| Scala | `sealed` | Subtypes must be in same file |
| Kotlin | `sealed` | Subtypes must be in same package and module |

### Exhaustive Pattern Matching

When all possible subtypes are known at compile time, the compiler can verify that pattern matching covers all cases:

| Language | Construct | No default needed |
|----------|-----------|-------------------|
| Java | `switch` expression | ✓ |
| Scala | `match` expression | ✓ |
| Kotlin | `when` expression | ✓ |

### Subtype Requirements

Each language has rules about what sealed subtypes must be:

| Language | Permitted Subtypes |
|----------|-------------------|
| Java | Must be `final`, `sealed`, or `non-sealed` |
| Scala | Case classes, objects, or other sealed traits |
| Kotlin | Data classes, objects, or other sealed classes/interfaces |

## The Solution: Sealed Payment Types

### Java Implementation

Java 17 introduced the `sealed` and `permits` keywords. Combined with records (Java 16+), we can create concise, immutable payment types:

```java
// PaymentMethod.java - Sealed interface restricts implementations
public sealed interface PaymentMethod permits CreditCard, BankTransfer, DigitalWallet {
    BigDecimal amount();
}

// CreditCard.java - Record implementing sealed interface
public record CreditCard(String cardNumber, YearMonth expiryDate, BigDecimal amount)
        implements PaymentMethod {
    public CreditCard {
        // Compact constructor for validation
        Objects.requireNonNull(cardNumber, "Card number cannot be null");
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Card number must be 16 digits");
        }
        // ... more validation
    }
}

// BankTransfer.java
public record BankTransfer(String iban, String bankCode, BigDecimal amount)
        implements PaymentMethod { /* validation */ }

// DigitalWallet.java
public record DigitalWallet(String provider, String accountId, BigDecimal amount)
        implements PaymentMethod { /* validation */ }
```

[View full Java implementation](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/payment)

### Scala Implementation

Scala's sealed traits and case classes provide the most concise syntax:

```scala
// PaymentMethod.scala - All subtypes must be in same file
sealed trait PaymentMethod:
  def amount: BigDecimal

case class CreditCard(cardNumber: String, expiryDate: YearMonth, amount: BigDecimal)
    extends PaymentMethod:
  require(cardNumber.length == 16 && cardNumber.forall(_.isDigit), "Card number must be 16 digits")
  require(!expiryDate.isBefore(YearMonth.now()), "Card has expired")
  require(amount > 0, "Amount must be positive")

case class BankTransfer(iban: String, bankCode: String, amount: BigDecimal)
    extends PaymentMethod:
  // validation with require()

case class DigitalWallet(provider: String, accountId: String, amount: BigDecimal)
    extends PaymentMethod:
  // validation with require()
```

[View full Scala implementation](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/payment)

### Kotlin Implementation

Kotlin's sealed interfaces with data classes offer a clean middle ground:

```kotlin
// PaymentMethod.kt - All implementations in same package/module
sealed interface PaymentMethod {
    val amount: BigDecimal
}

data class CreditCard(
    val cardNumber: String,
    val expiryDate: YearMonth,
    override val amount: BigDecimal,
) : PaymentMethod {
    init {
        require(cardNumber.length == 16 && cardNumber.all { it.isDigit() }) {
            "Card number must be 16 digits"
        }
        require(!expiryDate.isBefore(YearMonth.now())) { "Card has expired" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
    }
}

data class BankTransfer(val iban: String, val bankCode: String, override val amount: BigDecimal)
    : PaymentMethod { /* validation */ }

data class DigitalWallet(val provider: String, val accountId: String, override val amount: BigDecimal)
    : PaymentMethod { /* validation */ }
```

[View full Kotlin implementation](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/payment)

## Exhaustive Pattern Matching for Fee Calculation

The real power of sealed classes comes from exhaustive pattern matching. Let's implement fee calculation in all three languages.

### Java - Switch Expressions with Record Patterns

```java
public static BigDecimal calculateFee(PaymentMethod payment) {
    // No default needed - compiler verifies all cases are covered
    return switch (payment) {
        case CreditCard(var cardNumber, var expiry, var amount) ->
            amount.multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"));
        case BankTransfer(var iban, var bankCode, var amount) ->
            amount.compareTo(new BigDecimal("1000")) < 0
                ? new BigDecimal("2.50")
                : new BigDecimal("5.00");
        case DigitalWallet(var provider, var accountId, var amount) ->
            amount.multiply(new BigDecimal("0.025")).max(new BigDecimal("0.50"));
    };
}
```

Java 21 also supports **pattern guards** with the `when` keyword:

```java
public static String describeFee(PaymentMethod payment) {
    return switch (payment) {
        case CreditCard(var num, var exp, var amt) when amt.compareTo(new BigDecimal("100")) > 0
            -> "Credit card (high value): 2.9% + $0.30";
        case CreditCard(var num, var exp, var amt)
            -> "Credit card (standard): 2.9% + $0.30";
        case BankTransfer(var iban, var code, var amt) when amt.compareTo(new BigDecimal("1000")) >= 0
            -> "Bank transfer (high value): flat $5.00";
        case BankTransfer(var iban, var code, var amt)
            -> "Bank transfer (standard): flat $2.50";
        case DigitalWallet(var provider, var id, var amt)
            -> "Digital wallet (" + provider + "): 2.5% (min $0.50)";
    };
}
```

### Scala - Match Expressions with Case Class Destructuring

```scala
def calculateFee(payment: PaymentMethod): BigDecimal =
  payment match
    case CreditCard(_, _, amount)    =>
      amount * BigDecimal("0.029") + BigDecimal("0.30")
    case BankTransfer(_, _, amount)  =>
      if amount < BigDecimal("1000") then BigDecimal("2.50") else BigDecimal("5.00")
    case DigitalWallet(_, _, amount) =>
      (amount * BigDecimal("0.025")).max(BigDecimal("0.50"))
```

Scala uses `if` guards within patterns:

```scala
def describeFee(payment: PaymentMethod): String =
  payment match
    case CreditCard(num, _, amt) if amt > BigDecimal("100") =>
      s"Credit card (high value): 2.9% + $$0.30 on ${num.takeRight(4)}"
    case CreditCard(num, _, _) =>
      s"Credit card (standard): 2.9% + $$0.30 on ${num.takeRight(4)}"
    // ... other cases
```

### Kotlin - When Expressions with Smart Casting

```kotlin
fun calculateFee(payment: PaymentMethod): BigDecimal =
    when (payment) {
        is CreditCard ->
            payment.amount * BigDecimal("0.029") + BigDecimal("0.30")
        is BankTransfer ->
            if (payment.amount < BigDecimal("1000")) BigDecimal("2.50") else BigDecimal("5.00")
        is DigitalWallet ->
            (payment.amount * BigDecimal("0.025")).max(BigDecimal("0.50"))
    }
```

Kotlin maintains exhaustiveness by matching on the sealed type first, then using conditions within each branch:

```kotlin
fun describeFee(payment: PaymentMethod): String =
    when (payment) {
        is CreditCard ->
            if (payment.amount > BigDecimal("100")) {
                "Credit card (high value): 2.9% + \$0.30"
            } else {
                "Credit card (standard): 2.9% + \$0.30"
            }
        is BankTransfer ->
            if (payment.amount >= BigDecimal("1000")) {
                "Bank transfer (high value): flat \$5.00"
            } else {
                "Bank transfer (standard): flat \$2.50"
            }
        is DigitalWallet ->
            "Digital wallet (${payment.provider}): 2.5% (min \$0.50)"
    }
```

## Comparison Table

| Feature | Java 17+ | Scala 3 | Kotlin |
|---------|----------|---------|--------|
| Sealed declaration | `sealed interface/class ... permits` | `sealed trait/class` | `sealed interface/class` |
| Must list subtypes | Yes (`permits`) | No (same file) | No (same package/module) |
| Record/Data class | `record` | `case class` | `data class` |
| Pattern matching | `switch` expression | `match` expression | `when` expression |
| Destructuring | Record patterns | Case class patterns | Smart casting |
| Guards | `when` clause | `if` clause | Conditions in `when` |
| No default needed | ✓ | ✓ | ✓ |

## When to Use Sealed Classes

Sealed classes are ideal for:

1. **Domain modeling** - When you have a fixed set of variants (payment types, result types, states)
2. **State machines** - Each state is a subtype with specific data
3. **Result types** - Success/Error patterns with different payloads
4. **Command patterns** - Different commands with type-safe handling

## Best Practices

1. **Keep hierarchies small** - Sealed classes work best with 3-7 subtypes
2. **Use records/data classes** - Combine sealed with immutable data carriers
3. **Validate in constructors** - Ensure invariants at construction time
4. **Leverage exhaustiveness** - Don't add default cases; let the compiler help you
5. **Document the hierarchy** - Make the permitted subtypes clear

## Conclusion

Sealed classes bring type-safe ADT (Algebraic Data Type) patterns to Java, making it easier for Scala developers to apply familiar patterns. While Java's syntax is more verbose than Scala's, the concepts are nearly identical:

- Both use sealing to restrict subtypes
- Both enable exhaustive pattern matching
- Both support destructuring in patterns
- Both provide compile-time safety

The main difference is Java's explicit `permits` clause versus Scala's same-file requirement. Kotlin sits in between, requiring same package/module.

For Scala developers transitioning to Java, sealed classes combined with records provide the closest equivalent to sealed traits with case classes.

## Code Samples

See the complete implementations in our repository:
- [Java 21 Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/payment)
- [Scala 3 Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/payment)
- [Kotlin Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/payment)

---

*This is part of our Java 21 Interview Preparation series. Check out the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html) for more topics.*
