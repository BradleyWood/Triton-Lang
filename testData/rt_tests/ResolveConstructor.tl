package resolution

import java.lang.String

import org.junit.Assert

fun resolveConstructorTest() {
    Assert.assertEquals("Hello World", new String("Hello World"))
}

fun resolveConstructorFromFqnTest() {
    Assert.assertEquals("Hello World", new java.lang.String("Hello World"))
}

fun resolveFunctionOnConstructorTest() {
    Assert.assertEquals("Hello World", new String("Hello World").toString())
}
