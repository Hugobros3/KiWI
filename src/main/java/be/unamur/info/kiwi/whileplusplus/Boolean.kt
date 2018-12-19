package be.unamur.info.kiwi.whileplusplus

import be.unamur.info.kiwi.WhilePlusPlusDSLContext

object BooleanRoutines {
    val xorOperation: WhilePlusPlusDSLContext.() -> Unit = {
        val left = v("left")
        left assign readArrayCell(arguments.get(this), 0)

        val right = v("right")
        right assign readArrayCell(arguments.get(this), 1)

        If(left.get(this), {
            If(right.get(this), {
                returnValue assign False
            }) /* else */ {
                returnValue assign True
            }
        }) /* else */ {
            If(right.get(this), {
                returnValue assign True
            }) /* else */ {
                returnValue assign False
            }
        }
    }

    val notOperation: WhilePlusPlusDSLContext.() -> Unit = {
        If(readArrayCell(arguments.get(this), 0), {
            returnValue assign False
        }) /* else */ {
            returnValue assign True
        }
    }
}