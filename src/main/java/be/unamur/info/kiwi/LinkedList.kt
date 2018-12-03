package be.unamur.info.kiwi

import kotlin.Exception

/** Helper methods to use Value as a LinkedList */

/** Returns the i-th element of this linked list. Throws an exception if this isn't a big enough linked list */
operator fun Value.get(i: Int) : Value = when {
    i == 0 && this is Value.Pair -> a
    i > 0 && this is Value.Pair -> b[i - 1]
    else -> throw Exception("Out of bounds value: $i")
}

/** Creates a copy of the list with it's i-th element replaced with 'value' */
fun Value.replace(i: Int, value: Value) : Value {
    var cell = this
    val stack = mutableListOf<Value>()
    for(j in 0 until i) {
        val pair = (cell as? Value.Pair) ?: throw Exception("Out of bounds value: $i")
        stack.add(0, pair.a)
        cell = pair.b
    }

    val tail = (cell as? Value.Pair)?.b ?: throw Exception("Out of bounds value: $i")

    var modified = Value.Pair(value, tail)
    for(item in stack)
        modified = Value.Pair(item, modified)
    return modified
}

fun createEmptyList(size: Int) : Value = if(size > 0) Value.Pair(Value.Nil, createEmptyList(size - 1)) else Value.Nil

fun createlistOfAtoms(vararg values: A) : Value = values.foldRight(Value.Nil) { left, acc : Value ->
    Value.Pair(Value.Atom(left), acc)
}

/** Stringifies the linked list in a very readable way */
fun Value.prettyToString() : String = when(this) {
    Value.Nil -> "Nil"
    is Value.Atom -> "$value"
    is Value.Pair -> "(${a.prettyToString()}, ${b.prettyToString()})"
}