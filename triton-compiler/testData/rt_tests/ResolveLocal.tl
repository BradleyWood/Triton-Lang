package resolution

import org.junit.Assert

fun add(int a, int b): int {
    return a + b
}

fun resolveLocalFunction() {
    add(10, 20)

    int a = add(10, 20)
    long b = add(10, 20)

    Assert.assertEquals(30, a)
    Assert.assertEquals(30, b)

    float c = add(10, 20)
    double d = add(10, 20)

    Assert.assertEquals(30.0, c, 0.0001)
    Assert.assertEquals(30.0, d, 0.0001)
}

fun resolveLocalField() {
    int i = 0
    int j = 1
    Assert.assertEquals(1, add(i, j))
}
