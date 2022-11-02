package ru.smak.graphics

import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

fun testFunc(x: Float) = Color(
    abs(sin(10f*x)),
    abs(sin(5f*x)*cos(12*x)),
    abs(cos(24*x))
)