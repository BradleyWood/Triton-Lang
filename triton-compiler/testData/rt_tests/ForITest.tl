package test

import org.junit.Assert

fun countTest() {
    var count = 0

    for (var i = 0; i < 10; i += 1) {
        count += 1
    }

    Assert.assertEquals(10, count)
}

fun countTest2() {
    var count = 0

    var i = 1000
    for (i = 0; i < 10; i += 1) {
        count += 1
    }

    Assert.assertEquals(10, count)
}

fun countTest3() {
    var count = 0

    var i = 0
    for (; i < 10; i += 1, count += 1) {
        count += 1
    }

    Assert.assertEquals(20, count)
}
