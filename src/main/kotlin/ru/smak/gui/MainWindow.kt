package ru.smak.gui

import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.graphics.testFunc
import ru.smak.main
import ru.smak.math.Mandelbrot
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.GroupLayout
import javax.swing.JFrame

class MainWindow : JFrame() {
    private var firstPoint: Point? = null
    private var prevPoint: Point? = null
    val minSz = Dimension(600, 450)
    val mainPanel: GraphicsPanel
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = minSz

        val plane = Plane(-2.0, 1.0, -1.0, 1.0)
        val fp = FractalPainter(Mandelbrot()::isInSet, ::testFunc, plane)
        mainPanel = GraphicsPanel().apply {
            background = Color.WHITE
            addPainter(fp)
            addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent?) {
                    super.componentResized(e)
                    plane.width = width
                    plane.height = height
                }
            })
        }

        mainPanel.addMouseListener(object : MouseAdapter(){
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    firstPoint = it.point
                }
            }
            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                firstPoint?.let {first->
                    val g = mainPanel.graphics
                    g.color = Color.BLACK
                    g.setXORMode(Color.WHITE)
                    prevPoint?.let { prev ->
                        val psW = prev.x - first.x
                        val psH = prev.y - first.y
                        g.drawRect(first.x, first.y, psW, psH)
                    }
                    g.setPaintMode()
                }
                firstPoint = null
                prevPoint = null
            }
        })

        mainPanel.addMouseMotionListener(object : MouseAdapter(){
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                e?.let{ curr->
                    firstPoint?.let { first ->
                        val sW = curr.x - first.x
                        val sH = curr.y - first.y
                        val g = mainPanel.graphics
                        g.color = Color.BLACK
                        g.setXORMode(Color.WHITE)
                        prevPoint?.let {prev ->
                            val psW = prev.x - first.x
                            val psH = prev.y - first.y
                            g.drawRect(first.x, first.y, psW, psH)
                        }
                        g.drawRect(first.x, first.y, sW, sH)
                        g.setPaintMode()
                        prevPoint = curr.point
                    }
                }
            }
        })

        layout = GroupLayout(contentPane).apply {
            setHorizontalGroup(createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GROW, GROW, GROW)
                .addGap(8)
            )

            setVerticalGroup(createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GROW, GROW, GROW)
                .addGap(8)
            )
        }
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        mainPanel.graphics.run{
            setXORMode(Color.WHITE)
            drawLine(-100, -100, -101, -100)
            setPaintMode()
        }
    }

    companion object{
        const val GROW = GroupLayout.DEFAULT_SIZE
        const val SHRINK = GroupLayout.PREFERRED_SIZE
    }
}