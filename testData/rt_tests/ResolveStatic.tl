package resolution

import java.lang.String
import java.lang.System

import org.junit.Assert

fun resolveStaticFunctionFromImport() {
    Assert.assertEquals("10", String.valueOf(10))
}

fun resolveFullyQualifiedFunction() {
    Assert.assertEquals("10", java.lang.String.valueOf(10))
    Assert.assertEquals((java.lang.Integer) 10, java.lang.Integer.valueOf("10"))
}

fun resolveStaticField() {
    java.io.PrintStream ps = java.lang.System.out
    Assert.assertNotNull(ps)

    ps = System.out
    Assert.assertNotNull(ps)
}
