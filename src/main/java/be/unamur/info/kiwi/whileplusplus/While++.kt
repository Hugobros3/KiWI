package be.unamur.info.kiwi

import be.unamur.info.kiwi.whileplusplus.BooleanRoutines
import be.unamur.info.kiwi.whileplusplus.BooleanType
import be.unamur.info.kiwi.whileplusplus.readArrayCell

/**
 * While++ is just regular while with extra constructs that are basically fancy macros :)
 *
 * Reserved variables
 * VAR(0) STDIN: An array of input values
 * VAR(1) STDOUT: An array of output values
 * VAR(2) FIFO STACK: Internally used to store temporary values for conditions and whatnot
 */

fun createWhilePlusPlusProgram(p: (WhilePlusPlusDSLContext.() -> Unit)) =
    WhilePlusPlusDSLContext(null).apply(p)

class WhilePlusPlusDSLContext(val parentScope: WhilePlusPlusDSLContext?) {
    internal val whileProgram = WhileProgramParsingContext()
    internal fun toRegularWhile() = whileProgram.buildInstruction()

    internal val variables = mutableMapOf<String, Variable>()
    internal val returnValue = v("returnValue")
    internal val arguments = v("arguments")

    internal fun lookForVariable(name: String): Variable? = variables[name] ?: parentScope?.lookForVariable(name)

    /** Looks for a certain variable in this cope or a parent scope and creates it if not found.
     * Important: Note that variable names are supposed to be duplicated when to scopes aren't related ( in routine calls for instance ) */
    fun v(name: String): Evaluable.Settable.VariableReference {
        var variable = lookForVariable(name)
        if (variable == null) {
            //println("Creating variable $name")
            variable = Variable(name, this@WhilePlusPlusDSLContext, Expression.Var(globalVariableIndex++))
            variables[name] = variable
        }
        return Evaluable.Settable.VariableReference(variable)
    }

    val nil = Expression.Quote(Value.Nil)

    infix fun Evaluable.index(staticIndex: Int) = Evaluable.ArrayStaticIndexing(this, staticIndex)

    // ASSIGNATION LOGIC
    infix fun Evaluable.Settable.assign(evaluable: Evaluable) = when(this){
        is Evaluable.Settable.VariableReference -> {
            with(whileProgram) {
                variable.cell.assign(evaluable.get(this@WhilePlusPlusDSLContext))
            }
        }
    }

    // FLOW CONTROL
    private fun If(condition: Expression, instructions: WhilePlusPlusDSLContext.() -> Unit) = If(Evaluable.SimpleExpression(condition), instructions)
    fun If(condition: Evaluable, instructions: WhilePlusPlusDSLContext.() -> Unit) {
        val trueBranch = WhilePlusPlusDSLContext(this).apply(instructions).toRegularWhile()
        whileProgram.addInstructions(Conditionals.If(condition.get(this), trueBranch))
    }

    private fun If(condition: Expression, instructions: WhilePlusPlusDSLContext.() -> Unit,
           alternativeInstructions: (WhilePlusPlusDSLContext.() -> Unit)) = If(Evaluable.SimpleExpression(condition), instructions, alternativeInstructions)
    fun If(
        condition: Evaluable,
        instructions: WhilePlusPlusDSLContext.() -> Unit,
        alternativeInstructions: (WhilePlusPlusDSLContext.() -> Unit)
    ) {
        val trueBranch = WhilePlusPlusDSLContext(this).apply(instructions).toRegularWhile()
        val falseBranch = WhilePlusPlusDSLContext(this).apply(alternativeInstructions).toRegularWhile()

        whileProgram.addInstructions(Conditionals.IfElse(condition.get(this), trueBranch, falseBranch))
    }

    // BOOLEAN ROUTINES
    fun Evaluable.not(): Evaluable =
        Evaluable.RoutineCall(BooleanRoutines.notOperation, listOf(this.get(this@WhilePlusPlusDSLContext)))

    infix fun Evaluable.xor(right: Evaluable): Evaluable = Evaluable.RoutineCall(
        BooleanRoutines.xorOperation,
        listOf(this.get(this@WhilePlusPlusDSLContext), right.get(this@WhilePlusPlusDSLContext))
    )

    infix fun Evaluable.and(right: Evaluable): Evaluable = Evaluable.RoutineCall(
        BooleanRoutines.andOperation,
        listOf(this.get(this@WhilePlusPlusDSLContext), right.get(this@WhilePlusPlusDSLContext))
    )

    infix fun Evaluable.or(right: Evaluable): Evaluable = Evaluable.RoutineCall(
        BooleanRoutines.orOperation,
        listOf(this.get(this@WhilePlusPlusDSLContext), right.get(this@WhilePlusPlusDSLContext))
    )

    companion object {
        /** Current implementation is rather simple and just makes every variable global. */
        //TODO maybe some better management can be employed here
        var globalVariableIndex = 3
    }
}

class Routine(val instructions: WhilePlusPlusDSLContext.() -> Unit)

data class Variable(val name: String, val scope: WhilePlusPlusDSLContext, val cell: Expression.Var)

sealed class Evaluable {
    /** Allows you to use a rvalue and automagically cleans up behind you if needed */
    abstract fun get(context: WhilePlusPlusDSLContext): Expression

    data class SimpleExpression(private val expression: Expression) : Evaluable() {
        override fun get(context: WhilePlusPlusDSLContext) = expression
    }

    data class RoutineCall(val routine: WhilePlusPlusDSLContext.() -> Unit, val arguments2: List<Expression>? = null) :
        Evaluable() {

        /** To get the return value from a routine we first need to execute it ! */
        override fun get(context: WhilePlusPlusDSLContext): Expression {
            val routineInstance = createWhilePlusPlusProgram {

                // If this routine uses arguments, make sure to provide them in the routine's 'arguments' variable.
                if (arguments2 != null && arguments2.isNotEmpty()) {
                    var expression: Expression = Expression.Quote(Value.Nil)
                    for (argument in arguments2.reversed()) {
                        expression = Expression.Constructor(argument, expression)
                    }
                    arguments assign SimpleExpression(expression)
                }

                // Write out the routine code
                routine()
            }

            context.whileProgram.addInstructions(routineInstance.toRegularWhile())
            return routineInstance.returnValue.variable.cell
        }
    }

    data class ArrayStaticIndexing(val array: Evaluable, val index: Int) : Evaluable() {
        override fun get(context: WhilePlusPlusDSLContext): Expression {
            return readArrayCell(array.get(context), index)
        }
    }

    sealed class Settable : Evaluable() {
        data class VariableReference(val variable: Variable) : Settable() {
            override fun get(context: WhilePlusPlusDSLContext) : Expression = variable.cell
        }
        //data class IndexedArrayWriteable(val array: Settable, val index: Int) : Evaluable()
    }

}

/** Not a real stack: we can only reference the topmost element. */
internal object Fifo {
    val Top = Expression.Head(Expression.Var(2))

    fun Push(expression: Expression) = createProgram {
        v(2) assign cons(expression, v(2))
    }

    fun Pop() = createProgram {
        v(2) assign tl(v(2))
    }
}

object Conditionals {
    /** Special value used to break out of loops */
    val Neither = Expression.Constructor(BooleanType.TrueConst, BooleanType.FalseConst)

    fun If(condition: Expression, instructions: Instruction) = IfElse(condition, instructions, null)

    fun IfElse(condition: Expression, instructions: Instruction, alternativeInstructions: Instruction?) =
        createProgram {
            // Evaluate the condition ONCE and store it on stack
            addInstructions(Fifo.Push(condition))

            // Is the condition result 'true' ?
            //addInstructions(Fifo.Push(Expression.IsEqual(Fifo.Top, Boolean.TrueConst)))
            loop(Expression.IsEqual(Fifo.Top, BooleanType.TrueConst)) {
                addInstructions(Fifo.Pop())
                addInstructions(instructions)
                addInstructions(Fifo.Push(Neither))
            }

            if (alternativeInstructions != null) {
                // Is the condition result 'false' ?
                loop(Expression.IsEqual(Fifo.Top, BooleanType.FalseConst)) {
                    addInstructions(Fifo.Pop())
                    addInstructions(alternativeInstructions)
                    addInstructions(Fifo.Push(Neither))
                }
                //addInstructions(Fifo.Pop())
            }

            addInstructions(Fifo.Pop())
        }
}