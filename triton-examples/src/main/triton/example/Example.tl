package example

import java.util.List
import java.util.Collections
import java.util.ArrayList

fun main(String[] args) {
    List lst = new ArrayList()

    lst.add(54)
    lst.add(11)
    lst.add(71)
    lst.add(42)
    lst.add(34)
    lst.add(99)

    System.out.println(lst)

    Collections.sort(lst)

    System.out.println(lst)
}
