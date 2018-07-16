package resolution

import java.lang.String
import java.lang.System

fun resolveStaticFunctionFromImport() {
    String.valueOf(10)
}

fun resolveFullyQualifiedFunction() {
    java.lang.String.valueOf(10)
    java.lang.Integer.valueOf("10")
}

fun resolveStaticField() {
    java.io.PrintStream ps = java.lang.System.out
    ps = System.out
}
