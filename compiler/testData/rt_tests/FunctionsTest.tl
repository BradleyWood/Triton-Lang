package test

import org.junit.Assert

fun getValue(): int {
    return 100
}

fun testReturn() {
    Assert.assertEquals(100, getValue())
}
