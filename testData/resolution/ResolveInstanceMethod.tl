package resolution

import java.lang.String
import java.lang.Object
import java.lang.System

String fieldString = "World Hello"

fun resolveInstanceMethodOnLocal() {
    String str = "Hello, World"
    str.contains("Hello")
}

fun resolveInstanceMethodOnField() {
    fieldString.length()
}

fun resolveInstanceMethodOnExternalField() {
    System.out.getClass()
}

fun resolveFunctionOnFunctionExpr() {
    System.out.getClass().toString()
}

fun testFunctionOnObject() {
    Object o = ""
    o.getClass().toString()
    "".toString()
}
