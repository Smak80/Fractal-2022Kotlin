package ru.smak.graphics

import kotlinx.coroutines.*
import ru.smak.gui.Painter
import ru.smak.math.Complex
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import kotlin.concurrent.thread

class FractalPainter(
    var fractal: (Complex)->Float,
    var colorFunc: (Float)->Color,
    var plane: Plane,
) :
    Painter
{
    override var width: Int
        get() = plane.width
        set(value) {plane.width = value}
    override var height: Int
        get() = plane.height
        set(value) {plane.height = value}

    private val threadCount = Runtime.getRuntime().availableProcessors()
    private val pool = newFixedThreadPoolContext(threadCount, "PainterPool")
    override fun paint(g: Graphics) = runBlocking{
        //val bt = System.currentTimeMillis()
        repeat (width) { i ->
            launch(pool) {
                val pic = BufferedImage(1, height, BufferedImage.TYPE_INT_RGB)
                val picGr = pic.graphics
                for (j in 0..height) {
                    val res = fractal(
                        Complex(
                            Converter.xScrToCrt(i, plane),
                            Converter.yScrToCrt(j, plane)
                        )
                    )
                    val color = if (res == 1f) Color.BLACK
                    else colorFunc(res)
                    picGr.color = color
                    picGr.drawLine(0, j, 1, j)
                }
                g.drawImage(pic, i, 0, null)
            }
        }
        //val et = System.currentTimeMillis()
        //println(et-bt)
    }

}