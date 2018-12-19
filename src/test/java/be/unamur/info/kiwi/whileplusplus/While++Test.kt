package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.*
import org.junit.Test

class WhilePlusPlusTest {

    @Test
    fun testBooleans() {
        val program = createWhilePlusPlusProgram {
            v("maVariable") assign True
            v("maSuperVariable") assign False
            //v("youpie") assign (v("maVariable") and (v("maSuperVariable").not()))
            //v("youpie2") assign (v("maVariable").not() or (False))
            v("trois") assign (v("maVariable").not() xor v("maSuperVariable"))
        }

        //println(program)

        val kiwi = Kiwi(program.toRegularWhile())

        val result = kiwi.run(Value.Nil)

        println("Final state: ${result.prettyToString()}")
        for ((name, variable) in program.variables)
            if (!name.startsWith("_"))
                println("var $name = ${result[variable.cell.i].prettyToString()}")
    }

    fun generateRandomBooleanTest() {

    }
}