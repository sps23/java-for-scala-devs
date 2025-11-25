---
layout: post
title: "Pattern Matching in Java 21"
date: 2024-01-25 10:00:00 +0000
categories: features
tags: java scala pattern-matching
---

Pattern matching is one of Scala's killer features. Java has been steadily adding pattern matching capabilities, and Java 21 brings significant improvements. Let's explore!

## Type Patterns

**Scala:**
```scala
def describe(x: Any): String = x match {
  case s: String => s"String of length ${s.length}"
  case i: Int => s"Integer: $i"
  case _ => "Unknown"
}
```

**Java 21:**
```java
String describe(Object x) {
    return switch (x) {
        case String s -> "String of length " + s.length();
        case Integer i -> "Integer: " + i;
        default -> "Unknown";
    };
}
```

## Record Patterns

Java 21 introduces record patterns for destructuring records:

**Scala:**
```scala
case class Point(x: Int, y: Int)

def process(p: Point): String = p match {
  case Point(0, 0) => "Origin"
  case Point(x, 0) => s"On X-axis at $x"
  case Point(0, y) => s"On Y-axis at $y"
  case Point(x, y) => s"Point at ($x, $y)"
}
```

**Java 21:**
```java
record Point(int x, int y) {}

String process(Point p) {
    return switch (p) {
        case Point(int x, int y) when x == 0 && y == 0 -> "Origin";
        case Point(int x, int y) when y == 0 -> "On X-axis at " + x;
        case Point(int x, int y) when x == 0 -> "On Y-axis at " + y;
        case Point(int x, int y) -> "Point at (" + x + ", " + y + ")";
    };
}
```

## Guards

Both languages support guards (conditions in patterns):

**Scala:**
```scala
n match {
  case x if x > 0 => "positive"
  case x if x < 0 => "negative"
  case _ => "zero"
}
```

**Java 21:**
```java
switch (n) {
    case int x when x > 0 -> "positive";
    case int x when x < 0 -> "negative";
    default -> "zero";
}
```

## Sealed Types

For exhaustive pattern matching, both languages support sealed hierarchies:

**Scala 3:**
```scala
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)

def area(s: Shape): Double = s match
  case Shape.Circle(r) => Math.PI * r * r
  case Shape.Rectangle(w, h) => w * h
```

**Java 21:**
```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}

double area(Shape s) {
    return switch (s) {
        case Circle(double r) -> Math.PI * r * r;
        case Rectangle(double w, double h) -> w * h;
    };
}
```

## Conclusion

Java's pattern matching has come a long way! While not as concise as Scala's, it's now a powerful feature that Scala developers will feel comfortable using.

See more examples in our [GitHub repository](https://github.com/sps23/java-for-scala-devs).
