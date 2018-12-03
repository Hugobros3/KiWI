package be.unamur.info.kiwi

import org.junit.Test

class LinkedListTest {
    @Test
    fun testLinkedList() {
        val t1 = Value.Atom(1)
        val t2 = Value.Atom(2)
        val t3 = Value.Atom(3)
        val t4 = Value.Atom(4)

        val t2p = Value.Atom(-2)

        val myFancyList = Value.Pair(t1, Value.Pair(t2, Value.Pair(t3, Value.Pair(t4, Value.Nil))))
        println(myFancyList.prettyToString())

        val myFancyModifiedList = myFancyList.replace(1, t2p)
        println(myFancyModifiedList.prettyToString())
    }
}