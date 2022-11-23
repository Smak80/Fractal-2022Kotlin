package ru.smak.math

import java.awt.Color

class Mandelbrot {
    var r: Double = 2.0
    var maxIterations: Int = 200

    fun isInSet(c: Complex): Float{
        var cnt = 0
        var zn = Complex()
        val r2 = r * r
        while (++cnt <= maxIterations){
            zn = zn * zn + c
            if (zn.sqAbs >= r2)
                return cnt.toFloat() / maxIterations
        }
        return 1f
    }
}