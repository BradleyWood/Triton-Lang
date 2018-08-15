# Triton-Lang


## Goals

- JVM Interoperability
- Simplified Syntax
- Generics

## Project Layout

- [triton-compiler](/triton-compiler/src/main/java/org/bw/tl) - triton code compilation

- [triton-maven-plugin](/triton-maven-plugin/src/main/java/com/github/bradleywood) - build triton during maven compile phase

- [triton-examples](/triton-examples/src/main/raven/example) - Example triton projects built within maven

## Build

```
git clone https://github.com/BradleyWood/Triton-Lang.git
```

```
mvn install
```

## Examples

```kotlin
package test

fun main(String[] args) {
    for (String arg : args) {
        System.out.println(arg)
    }
}
```
