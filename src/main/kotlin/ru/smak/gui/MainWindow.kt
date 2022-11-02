package ru.smak.gui

import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.graphics.testFunc
import ru.smak.math.Mandelbrot
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.GregorianCalendar
import javax.swing.GroupLayout
import javax.swing.JFrame

class MainWindow : JFrame() {
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
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
            }
        })

        mainPanel.addMouseMotionListener(object : MouseAdapter(){
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
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

    companion object{
        const val GROW = GroupLayout.DEFAULT_SIZE
        const val SHRINK = GroupLayout.PREFERRED_SIZE
    }
}