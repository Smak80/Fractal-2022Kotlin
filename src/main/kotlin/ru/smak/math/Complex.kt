package ru.smak.math
import kotlin.math.abs
import kotlin.math.sqrt

class Complex(
    var re: Double,
    var im: Double,
) {
    constructor() : this(0.0, 0.0)
    constructor(re: Double): this(re, 0.0)

    override fun toString() = with(StringBuilder()) {
        if (!re.eq(0.0, 1e-10) ||
            im.eq(0.0, 1e-10)
        ) {
            append(re)
        }
        if (!im.eq(0.0, 1e-10)) {
            if (im < 0) append('-')
            else if (!re.eq(0.0, 1e-10)) append('+')
            if (!abs(im).eq(1.0))
                append(abs(im))
            append('i')
        }
        this
    }.toString()

    operator fun plus(other: Complex) =
        Complex(re + other.re, im + other.im)

    operator fun plusAssign(other: Complex){
        re += other.re
        im += other.im
    }

    operator fun minus(other: Complex) =
        Complex(re - other.re, im - other.im)

    operator fun minusAssign(other: Complex){
        re -= other.re
        im -= other.im
    }

    operator fun unaryMinus() =
        Complex(re, -im)

    operator fun times(other: Complex) =
        Complex(re * other.re - im * other.im, re * other.im + im * other.re)

    operator fun timesAssign(other: Complex){
        val oRe = re
        re = re * other.re - im * other.im
        im = oRe * other.im + im * other.re
    }

    operator fun times(lambda: Double) = Complex(lambda * re, lambda * im)

    operator fun div(other: Complex): Complex {
        val den: Double = other.sqAbs
        return Complex(
            (re * other.re + im * other.im) / den,
            (re * other.im - im * other.re) / den
        )
    }

    operator fun divAssign(other: Complex){
        val den: Double = other.sqAbs
        val oRe = re
        re = (re * other.re + im * other.im) / den
        im = (oRe * other.im - im * other.re) / den
    }

    val sqAbs: Double
        get() = re * re + im * im

    val abs: Double
        get() = sqrt(sqAbs)


}

fun Double.eq(other: Double, delta: Double = 0.0) =
    abs(this - other) < Math.ulp(this) + Math.ulp(other) + delta
