package be.unamur.info.kiwi

sealed class Expression {
    data class Var(val i: Int) : Expression()
    data class Quote (val a: A?) : Expression() {
        constructor(nil: Value.Nil) : this(null)
    }
    data class Constructor(val e: Expression, val f: Expression) : Expression()
    data class Head(val e: Expression) : Expression()
    data class Tail(val e: Expression) : Expression()
    data class IsEqual(val e: Expression, val f: Expression) : Expression()
}

sealed class Instruction {
    data class AssignEqual(val i : Int, val e: Expression) : Instruction()
    data class Sequence(val i: Instruction, val j: Instruction) : Instruction()
    data class While(val e: Expression, val i: Instruction) : Instruction()
}

sealed class Value {
    object Nil : Value()
    data class Atom(val value: A) : Value()
    data class Pair(val a: Value, val b: Value) : Value()
}

fun Expression.evaluate(store: Value): Value = when (this) {
    is Expression.Var -> store[i]
    is Expression.Quote -> if(a != null) Value.Atom(a) else Value.Nil
    is Expression.Constructor -> Value.Pair(e.evaluate(store), f.evaluate(store))
    is Expression.Head -> {
        when (val evaluated = e.evaluate(store)) {
            is Value.Pair -> evaluated.a
            else -> Value.Nil
        }
    }
    is Expression.Tail -> {
        when (val evaluated = e.evaluate(store)) {
            is Value.Pair -> evaluated.b
            else -> Value.Nil
        }
    }
    is Expression.IsEqual -> {
        if (e.evaluate(store) == f.evaluate(store))
            Value.Pair(Value.Nil, Value.Nil)
        else
            Value.Nil
    }
}

tailrec fun Instruction.execute(store: Value): Value = when (this) {
    is Instruction.AssignEqual -> store.replace(i, e.evaluate(store))
    is Instruction.Sequence -> j.execute(i.execute(store))

    // While instruction: Evaluate the condition
    is Instruction.While -> when (val condition = e.evaluate(store)) {
        is Value.Nil -> store
        // Re-execute this instruction (the while instruction) on the modified store
        else -> this.execute(i.execute(store))
    }
}