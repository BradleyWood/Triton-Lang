package test

import org.junit.Assert

fun ifExpressionTest() {
    var a = if (10 > 9) 9 else 10
    Assert.assertEquals(9, a)
}

fun ifExpressionTestIntDouble() {
    double a = if (10 > 9) 9 else 10.0
    Assert.assertEquals(9.0, a, 0.0001)
}

fun ifExpressionTestObj() {
    var a = if (10 > 9) "yes" else "no"
    Assert.assertEquals("yes", a)
}

fun ifStatementTest() {
    var a = 100

    if (a < 200) {
        return
    } else {
        Assert.fail("Unexpected branch execution")
    }

    Assert.fail()
}

fun ifStatementTest2() {
    var a = 200

    if (a < 200) {
        Assert.fail("Unexpected branch execution")
    } else {
        return
    }

    Assert.fail()
}
