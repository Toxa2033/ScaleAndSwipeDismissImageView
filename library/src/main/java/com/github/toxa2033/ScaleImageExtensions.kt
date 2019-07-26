package com.github.toxa2033

import java.math.RoundingMode

fun Float.roundToDigits(countDigit:Int=2): Float{
    return this.toBigDecimal().setScale(countDigit,RoundingMode.HALF_EVEN).toFloat()
}