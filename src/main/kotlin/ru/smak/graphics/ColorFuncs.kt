package ru.smak.graphics

import ru.smak.math.InterfaceColor
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

val defaultColor = InterfaceColor{x: Float -> Color(abs(cos(3f*x)), abs(sin(-5f*x)), abs(cos(4f+3f*x)))}
val secondColor = InterfaceColor{x: Float -> Color(abs(sin(10f*x)), abs(sin(5f*x)*cos(12*x)), abs(cos(24*x)))}
val thirdColor = InterfaceColor{x: Float -> Color(abs(cos(6f*x)), abs(cos(12f*x)), abs(sin(7f-7f*x)))}

val ColorFuncs = mutableListOf<InterfaceColor>(
    defaultColor,
    secondColor,
    thirdColor
)

