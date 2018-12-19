package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.Evaluable
import be.unamur.info.kiwi.Expression
import be.unamur.info.kiwi.Value
import be.unamur.info.kiwi.WhilePlusPlusDSLContext
import be.unamur.info.kiwi.whileplusplus.BooleanType.True
import be.unamur.info.kiwi.whileplusplus.BooleanType.False

object BooleanType {
    val TrueConst = Expression.Constructor(Expression.Quote(Value.Nil), Expression.Quote(Value.Nil))
    val FalseConst = Expression.Quote(Value.Nil)

    val True = Evaluable.SimpleExpression(BooleanType.TrueConst)
    val False = Evaluable.SimpleExpression(BooleanType.FalseConst)
}

object BooleanRoutines {
    val andOperation: WhilePlusPlusDSLContext.() -> Unit = {
        val left = v("left")
        left assign arguments.index(0)

        val right = v("right")
        right assign arguments.index(1)

        If(left, {
            If(right, {
                returnValue assign True
            }) /* else */ {
                returnValue assign False
            }
        }) /* else */ {
            returnValue assign False
        }
    }

    val orOperation: WhilePlusPlusDSLContext.() -> Unit = {
        val left = v("left")
        left assign arguments.index(0)

        val right = v("right")
        right assign arguments.index(1)

        If(left, {
            returnValue assign True
        }) /* else */ {
            If(right, {
                returnValue assign True
            }) /* else */ {
                returnValue assign False
            }
        }
    }

    val xorOperation: WhilePlusPlusDSLContext.() -> Unit = {
        val left = v("left")
        left assign arguments.index(0)

        val right = v("right")
        right assign arguments.index(1)

        If(left, {
            If(right, {
                returnValue assign False
            }) /* else */ {
                returnValue assign True
            }
        }) /* else */ {
            If(right, {
                returnValue assign True
            }) /* else */ {
                returnValue assign False
            }
        }
    }

    val notOperation: WhilePlusPlusDSLContext.() -> Unit = {
        If(arguments.index(0), {
            returnValue assign False
        }) /* else */ {
            returnValue assign True
        }
    }
}