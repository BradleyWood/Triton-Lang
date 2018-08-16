package triton

import java.util.List
import java.util.Scanner
import java.util.Collections

private Scanner scanner = new Scanner(System.in)

fun println() = System.out.println()

fun println(final String msg) = System.out.println(msg)

fun println(final Object obj) = System.out.println(obj)

fun println(final long l) = System.out.println(l)

fun println(final double d) = System.out.println(d)

fun print(final String msg) = System.out.print(msg)

fun print(final Object obj) = System.out.print(obj)

fun print(final long l) = System.out.print(l)

fun print(final double d) = System.out.print(d)

fun readLine() = scanner.nextLine()

fun readInt() = scanner.nextInt()

fun readBoolean() = scanner.nextBoolean()

fun readFloat() = scanner.nextFloat()

fun readDouble() = scanner.nextDouble()

fun sort(final List list): List {
    Collections.sort(list)
    return list
}
