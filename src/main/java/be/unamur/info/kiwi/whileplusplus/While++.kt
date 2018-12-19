package be.unamur.info.kiwi

import be.unamur.info.kiwi.whileplusplus.BooleanRoutines

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

    // VARIABLES STUFF

    fun v(name: String): RValue.VariableReference {
        var v = lookForVariable(name)
        if (v == null) {
            v = Variable(name, this@WhilePlusPlusDSLContext, Expression.Var(globalVariableIndex++))
            println("Creating variable $v")
            variables[name] = v
        }
        return RValue.VariableReference(v)
    }

    val nil = Expression.Quote(Value.Nil)

    infix fun RValue.VariableReference.assign(expression: Expression) = assign(RValue.SimpleExpression(expression))

    infix fun RValue.VariableReference.assign(rValue: RValue) {
        with(whileProgram) {
            variable.cell.assign(rValue.get(this@WhilePlusPlusDSLContext))
        }
    }

    // FLOW CONTROL

    fun If(condition: Expression, instructions: WhilePlusPlusDSLContext.() -> Unit) {
        val trueBranch = WhilePlusPlusDSLContext(this).apply(instructions).toRegularWhile()
        whileProgram.addInstructions(Conditionals.If(condition, trueBranch))
    }

    fun If(
        condition: Expression,
        instructions: WhilePlusPlusDSLContext.() -> Unit,
        alternativeInstructions: (WhilePlusPlusDSLContext.() -> Unit)
    ) {
        val trueBranch = WhilePlusPlusDSLContext(this).apply(instructions).toRegularWhile()
        val falseBranch = WhilePlusPlusDSLContext(this).apply(alternativeInstructions).toRegularWhile()

        whileProgram.addInstructions(Conditionals.IfElse(condition, trueBranch, falseBranch))
    }

    // BOOLEAN EXPRESSIONS
    val True = RValue.SimpleExpression(Boolean.TrueConst)
    val False = RValue.SimpleExpression(Boolean.FalseConst)

    infix fun RValue.and2(right: RValue): RValue {
        val left = this
        //val returnValue = v("_routine_call_${internalVarCounters++}")

        val routine : WhilePlusPlusDSLContext.() -> Unit = {
            If(left.get(this@WhilePlusPlusDSLContext), {
                If(right.get(this@WhilePlusPlusDSLContext), {
                    returnValue assign True
                }) /* else */ {
                    returnValue assign False
                }
            }) /* else */ {
                returnValue assign False
            }
        }

        return RValue.RoutineCall(routine)
    }

    infix fun RValue.or2(right: RValue): RValue {
        val left = this
        //val returnValue = v("_routine_call_${internalVarCounters++}")

        val routine : WhilePlusPlusDSLContext.() -> Unit = {
            If(left.get(this), {
                returnValue assign True
            }) /* else */ {
                If(right.get(this), {
                    returnValue assign True
                }) /* else */ {
                    returnValue assign False
                }
            }
        }

        return RValue.RoutineCall(routine)
    }

    fun RValue.not2(): RValue {
        val left = this
        //val returnValue = v("_routine_call_${internalVarCounters++}")

        val routine : WhilePlusPlusDSLContext.() -> Unit = {
            If(left.get(this), {
                returnValue assign False
            }) /* else */ {
                returnValue assign True
            }
        }

        return RValue.RoutineCall(routine)
    }

    fun RValue.not() : RValue = RValue.RoutineCall(BooleanRoutines.notOperation, listOf(this.get(this@WhilePlusPlusDSLContext)))

    infix fun RValue.xor(right: RValue): RValue = RValue.RoutineCall(BooleanRoutines.xorOperation, listOf(this.get(this@WhilePlusPlusDSLContext), right.get(this@WhilePlusPlusDSLContext)))

    companion object {
        /** Current implementation is rather simple and just makes every variable global. */
        //TODO maybe some better management can be employed here
        var globalVariableIndex = 3

        var internalVarCounters = 0
    }
}

class Routine(val instructions: WhilePlusPlusDSLContext.() -> Unit)

data class Variable(val name: String, val scope: WhilePlusPlusDSLContext, val cell: Expression.Var)

sealed class RValue {
    /** Allows you to use a rvalue and automagically cleans up behind you if needed */
    abstract fun get(context: WhilePlusPlusDSLContext): Expression

    data class SimpleExpression(private val expression: Expression) : RValue() {
        override fun get(context: WhilePlusPlusDSLContext) = expression
    }

    /** Inlines a routine call where it is used */
    data class RoutineCall(val routine: WhilePlusPlusDSLContext.() -> Unit, val arguments2: List<Expression>? = null) : RValue() {
        override fun get(context: WhilePlusPlusDSLContext): Expression {
            val routineInstance = createWhilePlusPlusProgram {
                if(arguments2 != null && arguments2.isNotEmpty()) {
                    println("Building arguments list")
                    var expression: Expression = Expression.Quote(Value.Nil)
                    for(argument in arguments2.reversed()) {
                        expression = Expression.Constructor(argument, expression)
                    }

                    arguments assign expression
                }
                routine()
            }

            context.whileProgram.addInstructions(routineInstance.toRegularWhile())
            return routineInstance.returnValue.variable.cell
        }
    }

    data class VariableReference(val variable: Variable) : RValue() {
        override fun get(context: WhilePlusPlusDSLContext) = variable.cell
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

object Boolean {
    val TrueConst = Expression.Constructor(Expression.Quote(Value.Nil), Expression.Quote(Value.Nil))
    val FalseConst = Expression.Quote(Value.Nil)
    val Neither = Expression.Constructor(TrueConst, FalseConst)
}

object Conditionals {
    fun If(condition: Expression, instructions: Instruction) = IfElse(condition, instructions, null)

    fun IfElse(condition: Expression, instructions: Instruction, alternativeInstructions: Instruction?) =
        createProgram {
            // Evaluate the condition ONCE and store it on stack
            addInstructions(Fifo.Push(condition))

            // Is the condition result 'true' ?
            //addInstructions(Fifo.Push(Expression.IsEqual(Fifo.Top, Boolean.TrueConst)))
            loop(Expression.IsEqual(Fifo.Top, Boolean.TrueConst)) {
                addInstructions(Fifo.Pop())
                addInstructions(instructions)
                addInstructions(Fifo.Push(Boolean.Neither))
            }
            //addInstructions(Fifo.Pop())

            if (alternativeInstructions != null) {
                // Is the condition result 'false' ?
                //addInstructions(Fifo.Push(Expression.IsEqual(Fifo.Top, Boolean.FalseConst)))
                loop(Expression.IsEqual(Fifo.Top, Boolean.FalseConst)) {
                    addInstructions(Fifo.Pop())
                    addInstructions(alternativeInstructions)
                    addInstructions(Fifo.Push(Boolean.Neither))
                }
                //addInstructions(Fifo.Pop())
            }

            addInstructions(Fifo.Pop())
        }
}