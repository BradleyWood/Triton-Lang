# Triton-Lang

Triton is a statically typed experimental programming language for the JVM.

## Goals

- Functional Programming
- JVM Interoperability
- Simplified Syntax
- String templates
- Type inference
- Scripting
- Generics

## Project Layout

- [triton-compiler](/triton-compiler/src/main/java/org/bw/tl) - triton code compilation

- [triton-maven-plugin](/triton-maven-plugin/src/main/java/com/github/bradleywood) - build triton during maven compile phase

- [triton-stdlib](/triton-stdlib/src/main/triton/triton) - The standard library

- [triton-examples](/triton-examples/src/main/triton/example) - Example triton projects built using the maven plugin

## Build

```
git clone https://github.com/BradleyWood/Triton-Lang.git
```

```
mvn clean install
```

## Examples

```kotlin
package test

fun main(String[] args) {
    for (var arg : args) {
        println(arg)
    }
}
```

### Functions

```kotlin
fun add(int a, int b): int {
    return a + b
}

fun sub(int a, int b) = a - b
```


### For

Foreach
```kotlin
fun display(String[] array) {
    for (var a : array) {
        println(a)
    }
}
```:q

For-I
```kotlin
fun count(int[] array): int {
    var count = 0
    
    for (var i = 0; i < array.length; i++) {
        count += array[i]
    }
    
    return count
}
```

Infinite for loop
```
for println("Infinte Loop")
```

```kotlin
for {
    println("Infinite loop")
}
```

While loop

```kotlin
while(a + b < 100) {
    println("$a + $b < 100")
    a += 3
    b -= 2
}
```
