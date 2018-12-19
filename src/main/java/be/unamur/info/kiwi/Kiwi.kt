package be.unamur.info.kiwi

/** For now let's only focus on While(Int), redefine this to get a While language on any set. */
typealias A = Int

fun main(args: Array<String>) {
    Kiwi(TODO())
}

/** KiWI: Kotlin While Interpreter */
class Kiwi(val program: Instruction) {

    fun run(input: Value?) : Value {
        var storeInitial = createEnvironment(program)

        if(input != null)
        storeInitial = storeInitial.replace(0, input)

        val storeFinal = program.execute(storeInitial)
        return storeFinal
    }

    private fun createEnvironment(program: Instruction): Value {
        var n = -1

        fun Expression.visit() : Unit = when(this) {
            is Expression.Var -> {
                if(i > n)
                    n = i
                Unit
            }
            is Expression.Quote -> Unit
            is Expression.Constructor -> {
                e.visit()
                f.visit()
            }
            is Expression.Head -> e.visit()
            is Expression.Tail -> e.visit()
            is Expression.IsEqual -> {
                e.visit()
                f.visit()
            }
        }

        fun Instruction.visit() : Unit = when(this) {
            is Instruction.AssignEqual -> {
                if(i > n)
                    n = i

                e.visit()
            }
            is Instruction.Sequence -> {
                i.visit()
                j.visit()
            }
            is Instruction.While -> {
                e.visit()
                i.visit()
            }
        }

        program.visit()
        //println("program accessed up to variable $n, creating a ${n+1}-sized linked list to host state")

        return createEmptyList(n + 1)
    }
}