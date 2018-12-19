package be.unamur.info.kiwi.whileplusplus.integers

import be.unamur.info.kiwi.*
import be.unamur.info.kiwi.whileplusplus.BooleanType

fun createWhileInt(byte: Int): Expression {
    var acc: Expression = Expression.Quote(Value.Nil)
    for (i in 0..7) {
        val bit = (byte shr i) and 0x1
        //println("bit $i: $bit")
        acc = Expression.Constructor(if (bit == 1) BooleanType.TrueConst else BooleanType.FalseConst, acc)
    }
    return acc
}

fun Value.interpretAsInt(): Int {
    var acc = 0
    for (i in 0..7) {
        val bit = this.get(7 - i) == BooleanType.TrueConst.evaluate(Value.Nil)
        //println("bit $i: $bit")
        if(bit)
            acc += (0x1 shl i)
    }
    return acc
}

object IntOperations {
    val addOperation: WhilePlusPlusDSLContext.() -> Unit = {
        val left = v("left")
        left assign arguments.index(0)

        val right = v("right")
        right assign arguments.index(1)


    }
}