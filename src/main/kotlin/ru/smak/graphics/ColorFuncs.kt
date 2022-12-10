package ru.smak.graphics

import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

val ColorFuncs = mutableListOf<(Float)->Color>(
    ::testFunc,
    ::yellowScheme,
)
fun testFunc(x: Float) = Color(
    abs(sin(10f*x)),
    abs(sin(5f*x)*cos(12*x)),
    abs(cos(24*x))
)

fun yellowScheme(x: Float) = Color(
    abs(cos(6f*x)),
    abs(cos(12f*x)),
    abs(sin(7f-7f*x))
)
