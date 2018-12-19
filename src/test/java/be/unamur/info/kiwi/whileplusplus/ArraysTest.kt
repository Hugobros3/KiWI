package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.*
import org.junit.Test

class ArraysTest {

    @Test
    fun testArrays() {
        val kiwi = Kiwi(createProgram {
            /*v(1) assign nil

            loop(v(0)) {
                v(1) assign (cons(hd(v(0)), v(1)))
                v(0) assign (tl(v(0)))
            }*/
            v(1) assign createArray(5)
            v(1) assign mutateArrayCell(v(1), 0, Expression.Quote(1))
            v(1) assign mutateArrayCell(v(1), 1, Expression.Quote(2))
            v(1) assign mutateArrayCell(v(1), 2, Expression.Quote(3))
            v(1) assign mutateArrayCell(v(1), 3, Expression.Quote(4))
            v(1) assign mutateArrayCell(v(1), 4, Expression.Quote(5))

            v(1) assign mutateArrayCell(v(1), 2, Expression.Quote(-3))
            //v(1) assign readArrayCell(v(1), 1)
        })

        val t1 = Value.Atom(1)
        val t2 = Value.Atom(2)
        val t3 = Value.Atom(3)
        val t4 = Value.Atom(4)

        val myFancyList = Value.Pair(t1, Value.Pair(t2, Value.Pair(t3, Value.Pair(t4, Value.Nil))))
        println("Initial state: ${myFancyList.prettyToString()}")

        val result = kiwi.run(myFancyList)

        println("Final state: ${result[1].prettyToString()}")
    }
}