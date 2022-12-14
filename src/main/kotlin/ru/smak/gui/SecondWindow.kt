package ru.smak.gui

import ru.smak.graphics.Converter
import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.gui.MainWindow.Companion.GROW
import ru.smak.math.Julia
import ru.smak.math.Mandelbrot
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class SecondWindow(colorScheme: (Float) -> Color) : JFrame() {
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(600, 450)
    val secondPanel: GraphicsPanel
    val trgsz = TargetSz()
    private var startPoint: Point? = null
    private var numButtonPressed: Int = 0

    init {

        defaultCloseOperation = DISPOSE_ON_CLOSE
        minimumSize = minSz

        val plane = Plane(-2.0, 1.0, -1.0, 1.0)
        val fpj = FractalPainter(Julia()::isInSet, colorScheme, plane)
        trgsz.getTargetFromPlane(plane)
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
                    if (it.button == MouseEvent.BUTTON1)
                        rect.addPoint(it.point)
                    else if (it.button == MouseEvent.BUTTON3)
                        startPoint = it.point
                    numButtonPressed = it.button
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                if (numButtonPressed == MouseEvent.BUTTON1)
                {
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
                            makeOneToOne(plane,x1,x2,y1,y2,secondPanel.size,trgsz)//Делает панель мастштабом 1 к 1 и меняет trgsz
                            secondPanel.repaint()
                        }
                    }
                    rect.destroy()
                } else if(numButtonPressed == MouseEvent.BUTTON3)
                {
                    startPoint = null
                }
                numButtonPressed = 0
            }
        })

        secondPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                if (numButtonPressed == MouseEvent.BUTTON1) {
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
                        }}
                }
                else if (numButtonPressed == MouseEvent.BUTTON3)
                {
                    if (e != null) {
                        startPoint?.let {
                            val shiftX = Converter.xScrToCrt(e.x,plane) - Converter.xScrToCrt(it.x,plane)
                            val shiftY = Converter.yScrToCrt(e.y,plane) - Converter.yScrToCrt(it.y,plane)
                            trgsz.shiftImage(shiftX, shiftY, plane)
                            makeOneToOne(plane,trgsz, secondPanel.size)
                            startPoint = e.point
                            secondPanel.repaint()
                        }
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