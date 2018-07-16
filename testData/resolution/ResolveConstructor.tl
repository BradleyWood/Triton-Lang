package resolution

import java.lang.String

fun resolveConstructor() {
    new String("Hello World")
    String str = new String("Hello World")
    str.toString()
}

fun resolveConstructorFromFqn() {
    new java.lang.String("Hello World")
    String str = new java.lang.String("Hello World")
    str.toString()
}

fun resolveFunctionOnConstructor() {
    new String("Hello World").toString()
}
