package triton

import java.util.List
import java.util.Scanner
import java.util.Collections

private Scanner scanner = new Scanner(System.in)

fun println(String msg) {
    System.out.println(msg)
}

fun println(Object obj) {
    System.out.println(obj)
}

fun println(long l) {
    System.out.println(l)
}

fun println(double d) {
    System.out.println(d)
}

fun println() {
    System.out.println()
}

fun print(String msg) {
    System.out.print(msg)
}

fun print(Object obj) {
    System.out.print(obj)
}

fun print(long l) {
    System.out.print(l)
}

fun print(double d) {
    System.out.print(d)
}

fun sort(List list): List {
    Collections.sort(list)
    return list
}

fun readLine(): String {
    return scanner.nextLine()
}

fun readInt(): int {
    return scanner.nextInt()
}

fun readBoolean(): boolean {
    return scanner.nextBoolean()
}

fun readFloat(): float {
    return scanner.nextFloat()
}

fun readDouble(): double {
    return scanner.nextDouble()
}
