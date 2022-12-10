package ru.smak.gui

import ru.smak.graphics.Converter
import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.graphics.testFunc
import ru.smak.gui.MainWindow.Companion.GROW
import ru.smak.math.Julia
import ru.smak.math.Mandelbrot
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SecondWindow() : JFrame() {
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(600, 450)
    val secondPanel: GraphicsPanel

    init {

        defaultCloseOperation = DISPOSE_ON_CLOSE
        minimumSize = minSz

        val plane = Plane(-2.0, 1.0, -1.0, 1.0)
        val fpj = FractalPainter(Julia()::isInSet, ::testFunc, plane)
        secondPanel = GraphicsPanel().apply {
            background = Color.WHITE
            addPainter(fpj)
            addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent?) {
                    super.componentResized(e)
                    plane.width = width
                    plane.height = height
                }
            })
        }

        secondPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    rect.addPoint(it.point)
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                rect.leftTop?.let { first ->
                    val g = secondPanel.graphics
                    g.color = Color.BLACK
                    g.setXORMode(Color.WHITE)
                    g.drawRect(first.x, first.y, rect.width, rect.height)
                    g.setPaintMode()
                    if (rect.isExistst) {
                        val x1 = rect.x1?.let { Converter.xScrToCrt(it, plane) } ?: return@let
                        val x2 = rect.x2?.let { Converter.xScrToCrt(it, plane) } ?: return@let
                        val y1 = rect.y1?.let { Converter.yScrToCrt(it, plane) } ?: return@let
                        val y2 = rect.y2?.let { Converter.yScrToCrt(it, plane) } ?: return@let
                        plane.xEdges = Pair(x1, x2)
                        plane.yEdges = Pair(y1, y2)
                        secondPanel.repaint()
                    }
                }
                rect.destroy()
            }
        })

        secondPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                e?.let { curr ->
                    rect.leftTop?.let { first ->
                        val g = secondPanel.graphics
                        g.color = Color.BLACK
                        g.setXORMode(Color.WHITE)
                        if (rect.isExistst)
                            g.drawRect(first.x, first.y, rect.width, rect.height)
                        rect.addPoint(curr.point)
                        rect.leftTop?.let { f -> g.drawRect(f.x, f.y, rect.width, rect.height) }
                        g.setPaintMode()
                    }
                }
            }
        })


        layout = GroupLayout(contentPane).apply {
            setHorizontalGroup(
                createSequentialGroup()
                    .addGap(8)
                    .addComponent(secondPanel, GROW, GROW, GROW)
                    .addGap(8)
            )

            setVerticalGroup(
                createSequentialGroup()
                    .addGap(8)
                    .addComponent(secondPanel, GROW, GROW, GROW)
                    .addGap(8)
            )
        }
    }
}