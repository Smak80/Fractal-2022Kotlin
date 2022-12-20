package ru.smak.math

class Julia : Mandelbrot() {
    companion object {
        var selectedPoint = Complex(0.0,0.0)
    }

    override fun IsInSet(c : Complex) : Float {
        var cnt = 0
        var zn = c
        val r2 = r * r
        while (++cnt <= maxIterations){
            zn = zn * zn + selectedPoint
            if (zn.sqAbs >= r2)
                return cnt.toFloat() / maxIterations
        }
        return 1f
    }
}