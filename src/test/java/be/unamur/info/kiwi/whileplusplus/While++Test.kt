package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.*
import be.unamur.info.kiwi.whileplusplus.BooleanType.False
import be.unamur.info.kiwi.whileplusplus.BooleanType.True
import org.junit.Test

class WhilePlusPlusTest {

    @Test
    fun testBooleans() {
        val program = createWhilePlusPlusProgram {
            v("maVariable") assign True
            v("maSuperVariable") assign False
            v("youpie") assign (v("maVariable") and (v("maSuperVariable").not()))
            v("youpie2") assign (v("maVariable").not() or (False))
            v("trois") assign (v("maVariable").not() xor v("maSuperVariable").not())

            v("nombre") assign Evaluable.SimpleExpression(Expression.Quote(5))
            If(v("trois")) {
                v("nombre") assign Evaluable.SimpleExpression(Expression.Quote(10))
            }
        }


        val kiwi = Kiwi(program.toRegularWhile())
        val result = kiwi.run(Value.Nil)

        println("Final state: ${result.prettyToString()}")

        for ((name, variable) in program.variables)
            if (!name.startsWith("_"))
                println("var $name = ${result[variable.cell.i].prettyToString().let { if(it == "Nil") "False" else if(it == "(Nil, Nil)") "True" else it}}")

        println("Program size (toString() on the final Instruction): ${program.toRegularWhile().toString().length} bytes")
        //println(program)
    }
}