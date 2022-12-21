package ru.smak.math.video

import ru.smak.math.Complex
import kotlin.math.pow

fun lerpUnclamped(p0: Complex, p1: Complex, t: Double): Complex {
    return p0 + (p1 - p0) * t
}

fun lerpUnclamped(p0: Double, p1: Double, t: Double): Double {
    return p0 + (p1 - p0) * t
}

fun smoothOutExp(p0: Double, p1: Double, t: Double): Double {
    val param = if (t == 1.0) 1.0 else -(2.0.pow(-10*t)) + 1.0009765625
    return p0 + (p1 - p0) * param
}