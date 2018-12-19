package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.*
import be.unamur.info.kiwi.whileplusplus.integers.createWhileInt
import be.unamur.info.kiwi.whileplusplus.integers.interpretAsInt
import org.junit.Test

class TestIntRepresentation {
    @Test
    fun testIntStaticTranslation() {
        val kiwi = Kiwi(createProgram {
            v(0) assign v(0)
        })

        val value37 = createWhileInt(37).evaluate(Value.Nil)
        println("37 = ${value37.prettyToString()} = ${value37.interpretAsInt()}")
    }
}