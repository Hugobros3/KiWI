package be.unamur.info.kiwi

/** Allows for writing While programs in a simpler syntax without going overboard and pulling out a heavy tool
 * like ANTLR */
class WhileProgramParsingContext {
    private val instructions = mutableListOf<Instruction>()

    val nil = Expression.Quote(Value.Nil)

    fun v(i: Int) = Expression.Var(i)
    fun cons(left: Expression, right: Expression) = Expression.Constructor(left, right)
    fun hd(expression: Expression) : Expression = Expression.Head(expression)
    fun tl(expression: Expression) : Expression = Expression.Tail(expression)

    infix fun Expression.Var.assign(e: Expression) {
        instructions += Instruction.AssignEqual(this.i, e)
    }

    fun loop(condition: Expression, body: WhileProgramParsingContext.() -> Unit) {
        val instruction = WhileProgramParsingContext().apply(body).buildInstruction()
        instructions += Instruction.While(condition, instruction)
    }

    /** Compresses the list of instruction given into a single, possibly complex, instruction */
    fun buildInstruction() : Instruction = when {
        instructions.size <= 0 -> throw Exception("You need instructions !")
        instructions.size == 1 -> instructions[0]
        else -> instructions.reduceRight { left, right -> Instruction.Sequence(left, right) }
    }

    fun addInstructions(vararg instructions: Instruction) {
        this.instructions.addAll(instructions)
    }
}

fun createProgram(programCode: (WhileProgramParsingContext.() -> Unit)) = WhileProgramParsingContext().apply(programCode).buildInstruction()