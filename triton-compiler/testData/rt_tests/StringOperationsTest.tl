package test

import org.junit.Assert

fun strAddStrTest() {
    Assert.assertEquals("ab", "a" + "b")
    Assert.assertEquals("ba", "b" + "a")
}

fun strIntAddTest() {
    Assert.assertEquals("a10", "a" + 10)
    Assert.assertEquals("10a", 10 + "a")
}

fun strBoolAddTest() {
    Assert.assertEquals("atrue", "a" + true)
    Assert.assertEquals("truea", true + "a")
}

fun strFloatAddTest() {
    Assert.assertEquals("a1.0", "a" + 1.0f)
    Assert.assertEquals("1.0a", 1.0f + "a")
}

fun strDoubleAddTest() {
    Assert.assertEquals("a1.0", "a" + 1.0)
    Assert.assertEquals("1.0a", 1.0 + "a")
}

fun testStrAddObj() {
    java.lang.Integer i = 100
    Assert.assertEquals("a100", "a" + i)
    Assert.assertEquals("100a", i + "a")
}

fun testDeepStringAdd() {
    Assert.assertEquals("abcd", "a" + "b" + "c" + "d")
    Assert.assertEquals("abcd", ("a" + "b") + ("c" + "d"))
}

fun testAddWithSubExpressions() {
    Assert.assertEquals("abcd5050", "a" + "b" + "c" + "d" + 50 + 50)
    Assert.assertEquals("abcd100", "a" + "b" + "c" + "d" + (50 + 50))
}
