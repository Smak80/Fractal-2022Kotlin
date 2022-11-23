package ru.smak.graphics

import ru.smak.gui.Painter
import ru.smak.math.Complex
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import kotlin.concurrent.thread

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
        val bt = System.currentTimeMillis()
        val threadCount = Runtime.getRuntime().availableProcessors()
        val pWidth = width / threadCount + 1
        List(threadCount) { threadNum ->
            thread {
                val currPicWidth = pWidth -
                            if (threadCount == threadNum + 1) width % threadCount else 0
                val shift = threadNum * pWidth
                val pic = BufferedImage(currPicWidth, height, BufferedImage.TYPE_INT_RGB)
                val picGr = pic.graphics
                for (i in 0..currPicWidth) {
                    for (j in 0..height) {
                        val res = fractal(
                            Complex(
                                Converter.xScrToCrt(i + shift, plane),
                                Converter.yScrToCrt(j, plane)
                            )
                        )
                        val color = if (res == 1f) Color.BLACK
                                    else colorFunc(res)
                        picGr.color = color
                        picGr.drawLine(i, j, i + 1, j)
                    }
                }
                synchronized(g){
                    g.drawImage(pic, shift, 0, null)
                }
            }
        }.forEach { it.join() }
        val et = System.currentTimeMillis()
        println(et - bt)
    }

}