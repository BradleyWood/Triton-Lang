package resolution

import java.lang.String
import java.lang.Object
import java.lang.System

import org.junit.Assert

String fieldString = "World Hello"

fun resolveInstanceMethodOnLocalTest() {
    String str = "Hello, World"
    Assert.assertTrue(str.contains("Hello"))
}

fun resolveInstanceMethodOnFieldTest() {
    Assert.assertEquals(11, fieldString.length())
}

fun resolveInstanceMethodOnExternalFieldTest() {
    Assert.assertNotNull(System.out.getClass())
}

fun resolveFunctionOnFunctionExprTest() {
    Assert.assertNotNull(System.out.getClass().toString())
}

fun testFunctionOnObjectTest() {
    Object o = ""

    Assert.assertEquals("class java.lang.String", o.getClass().toString())
    Assert.assertEquals("", "".toString())
}
