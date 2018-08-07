package test

import org.junit.Assert

fun testForEach() {
    int[] ai = new int[4];

    ai[0] = 1
    ai[1] = 2
    ai[2] = 3
    ai[3] = 4

    int total = 0

    for (var a : ai) {
        total += a
    }

    for (int a : ai) {
        total += a
    }

    Assert.assertEquals(20, total)
}
