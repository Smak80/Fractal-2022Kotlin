package ru.smak.tools

import java.io.Serializable

data class FractalData(
    var xMin: Double,
    var xMax: Double,
    var yMin: Double,
    var yMax: Double,
    var fractalFuncIndex: Int,
    var colorFuncIndex: Int,
    var isDynamical: Boolean,
    var maxIterations: Int
) : Serializable