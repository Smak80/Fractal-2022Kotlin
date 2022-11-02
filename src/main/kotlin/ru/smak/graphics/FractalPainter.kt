package ru.smak.graphics

import ru.smak.gui.Painter
import ru.smak.math.Complex
import java.awt.Color
import java.awt.Graphics

class FractalPainter(
    var fractal: (Complex)->Float,
    var colorFunc: (Float)->Color,
    val plane: Plane,
) :
    Painter
{
    override var width: Int
        get() = plane.width
        set(value) {plane.width = value}
    override var height: Int
        get() = plane.height
        set(value) {plane.height = value}

    override fun paint(g: Graphics) {
        for (i in 0..width){
            for (j in 0..height){
                val res = fractal(Complex(
                        Converter.xScrToCrt(i, plane),
                        Converter.yScrToCrt(j, plane)
                    ))
                if (res == 1f) g.color = Color.BLACK
                else g.color = colorFunc(res)
                g.drawLine(i, j, i+1, j)
            }
        }
    }

}