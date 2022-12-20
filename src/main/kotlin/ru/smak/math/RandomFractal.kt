package ru.smak.math

class RandomFractal: Fractal{
    var r: Double = 2.0
    var maxIterations: Int = 200

    override fun IsInSet(c: Complex): Float{
        var cnt = 0
        var zn = Complex()
        val r2 = r * r
        while (++cnt <= maxIterations){
            //zn = zn - (zn-Complex(1.0,0.0))*(zn-Complex(1.0,0.0))*(zn-Complex(1.0,0.0))/
                    //(zn*zn*3.0) + c
            zn = zn*zn*zn*zn+c
            if (zn.sqAbs >= r2)
                return cnt.toFloat() / maxIterations
        }
        return 1f
    }

}