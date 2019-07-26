package ru.test.library

import java.math.BigDecimal
import java.math.RoundingMode

fun Float.roundToDigits(countDigit:Int=2): Float{
    return this.toBigDecimal().setScale(countDigit,RoundingMode.HALF_EVEN).toFloat()
}