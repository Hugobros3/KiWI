package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.Expression
import be.unamur.info.kiwi.Value
import be.unamur.info.kiwi.WhilePlusPlusDSLContext

fun createArray(size: Int) : Expression {
    var cell: Expression = Expression.Quote(Value.Nil)
    for(i in 0 until size)
        cell = Expression.Constructor(Expression.Quote(Value.Nil), cell)
    return cell
}

fun readArrayCell(array: Expression, indexConstant: Int) : Expression {
    var cell = array
    for(i in 0 until indexConstant)
        cell = Expression.Tail(cell)
    return Expression.Head(cell)
}

fun mutateArrayCell(originalArray: Expression, indexConstant: Int, newValue: Expression) : Expression {
    var cell = originalArray
    for(i in 0 until indexConstant)
        cell = Expression.Tail(cell)

    val oldTail = Expression.Tail(cell)
    val middle = Expression.Constructor(newValue, oldTail)
    var newArray = middle

    for(i in (indexConstant - 1) downTo 0)
        newArray = Expression.Constructor(readArrayCell(originalArray, i), newArray)

    return newArray
}