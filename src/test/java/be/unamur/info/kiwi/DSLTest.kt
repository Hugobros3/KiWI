package be.unamur.info.kiwi

import org.junit.Test

class DSLTest {
    @Test
    /** Tests the custom Kotlin DSL I use works */
    fun testDSL() {
        val dslVersion: WhileProgramParsingContext.() -> Unit = {
            v(1) assign nil

            loop(v(0)) {
                v(1) assign (cons(hd(v(0)), v(1)))
                v(0) assign (tl(v(0)))
            }
        }

        val handwrittenTranslation =
            Instruction.Sequence(
                Instruction.AssignEqual(1, Expression.Quote(Value.Nil)),
                Instruction.While(
                    Expression.Var(0), Instruction.Sequence(
                        Instruction.AssignEqual(
                            1,
                            Expression.Constructor(Expression.Head(Expression.Var(0)), Expression.Var(1))
                        ),
                        Instruction.AssignEqual(0, Expression.Tail(Expression.Var(0)))
                    )
                )
            )

        val dslTranslated = dslVersion.buildProgram()

        println(dslTranslated)
        println(handwrittenTranslation)

        check(dslTranslated == handwrittenTranslation)
    }
}