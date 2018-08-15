package ReturnTest

fun validVoidMethod() {

}

fun validVoidMethod2() {
    test()
    test()
    return
}

fun validIntMethod(): int {
    return 1
}

fun validIntMethod2(): int {
    test()
    if (true) {
        return 10
    } else {
    }

    test()

    return 10
}

fun validIntMethod3(): int {
    if (true) {
        return 10
    } else if (false) {
        return 10
    } else {
        return 10
    }
}

fun validIntMethod4(): int {
    if (true)
        return 10
    else if (false)
        return 10
    else
        return 10
}

fun validIntMethod5(): int {
    if (true)
        return 10
    else if (false)
        return 10
    else
        return 10

    return 10
}

// nested test
fun validIntMethod6(): int {
    if (true) {
        return 10
    } else if (false) {
        if (true) {
            test()
            return 10
        } else if (false) {
            test()
            return 10
        } else {
            test()
            if (true) {
                return 10
            } else {
                return 10
            }
        }
    } else {
        return 10
    }
}


fun failIntMethod(): int {

}

fun failIntMethod2(): int {
    test()
    if (true) {
        return 10
    } else {
    }
}

fun failIntMethod2(): int {
    test()
    if (true) {
        return 10
    } else {
        return 10
    }
    // dead code
    test()
}

fun failIntMethod2(): int {
    test()
    if (true) {
        return 10
    } else if(false) {
        return 10
    } else if (true) {
        return 10
    } else {
    }
}
