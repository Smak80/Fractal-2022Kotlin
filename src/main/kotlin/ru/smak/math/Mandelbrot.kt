package ru.smak.math

open class Mandelbrot {
    companion object{
    var r: Double = 2.0
    var maxIterations: Int = 200
                    }

    open fun isInSet(c: Complex): Float{
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