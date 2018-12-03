package be.unamur.info.kiwi

import org.junit.Test

class KiwiTest {

    @Test
    fun `Test List reverse`() {
        val kiwi = Kiwi(createProgram {
            v(1) assign nil

            loop(v(0)) {
                v(1) assign (cons(hd(v(0)), v(1)))
                v(0) assign (tl(v(0)))
            }
        })

        val t1 = Value.Atom(1)
        val t2 = Value.Atom(2)
        val t3 = Value.Atom(3)
        val t4 = Value.Atom(4)

        val myFancyList = Value.Pair(t1, Value.Pair(t2, Value.Pair(t3, Value.Pair(t4, Value.Nil))))
        println("Initial state: ${myFancyList.prettyToString()}")

        val result = kiwi.run(myFancyList)

        println("Final state: ${result.prettyToString()}")
    }

    @Test
    fun `Test Concatenation`() {
        val program = createProgram {
            v(1) assign hd(v(0))
            v(2) assign tl(v(0))
            v(3) assign nil

            loop(v(1)) {
                v(3) assign cons(hd(v(1)), v(3))
                v(1) assign tl(v(1))
            }

            v(1) assign v(2)

            loop(v(3)) {
                v(1) assign cons(hd(v(3)), v(1))
                v(3) assign tl(v(3))
            }
        }

        val listA = createlistOfAtoms(1, 2, 3, 4, 5)
        val listB = createlistOfAtoms(6, 7, 8, 9, 10)

        val kiwi = Kiwi(program)

        println(kiwi.run(Value.Pair(listA, listB)).prettyToString())
    }
}