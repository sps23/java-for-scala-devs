---
layout: post
title: "Java Records vs Scala Case Classes"
description: "Compare Java 16+ Records with Scala case classes - learn about immutability, auto-generated methods, pattern matching, and key differences between these immutable data types."
date: 2025-11-23 11:00:00 +0000
categories: [features]
tags: [java, scala, records, case-classes]
---

One of the most beloved features of Scala is the case class. Java 16+ introduced Records, which provide similar functionality. Let's compare them!

## Scala Case Class

```scala
case class Person(name: String, age: Int)

// Usage
val person = Person("Alice", 30)
println(person.name)       // Alice
println(person)            // Person(Alice,30)
person.copy(age = 31)      // Person(Alice,31)
```

## Java Record

```java
public record Person(String name, int age) {}

// Usage
var person = new Person("Alice", 30);
System.out.println(person.name());  // Alice
System.out.println(person);         // Person[name=Alice, age=30]
// No built-in copy method, but you can create one
```

## Key Differences

| Feature | Scala Case Class | Java Record |
|---------|-----------------|-------------|
| Immutable | Yes | Yes |
| Auto-generated equals/hashCode | Yes | Yes |
| Auto-generated toString | Yes | Yes |
| Copy method | Built-in | Manual |
| Pattern matching | Yes | Yes (Java 21+) |
| Inheritance | Limited | No (final) |

## Pattern Matching Example

**Scala:**
```scala
person match {
  case Person(name, age) if age >= 18 => s"$name is an adult"
  case Person(name, _) => s"$name is a minor"
}
```

**Java 21:**
```java
switch (person) {
    case Person(String name, int age) when age >= 18 -> name + " is an adult";
    case Person(String name, int age) -> name + " is a minor";
}
```

## Conclusion

Java Records are a welcome addition for Scala developers. While they lack some features like the `copy` method, they provide a clean, concise way to define immutable data carriers.

Check out the [java21 module](https://github.com/sps23/java-for-scala-devs/tree/main/java21) for more examples!
