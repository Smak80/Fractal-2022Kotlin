package ru.smak.gui

import ru.smak.graphics.Converter
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
import javax.swing.*


class MainWindow : JFrame() {
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(600, 450)
    val mainPanel: GraphicsPanel

    init {
        val menuBar = JMenuBar().apply {
            add(createColorMenu())
            add(createCtrlZButton())
            add(createAboutButton())
        }

        jMenuBar = menuBar

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

        mainPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    rect.addPoint(it.point)
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                rect.leftTop?.let { first ->
                    val g = mainPanel.graphics
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
                        mainPanel.repaint()
                    }
                }
                rect.destroy()
            }
        })

        mainPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                e?.let { curr ->
                    rect.leftTop?.let { first ->
                        val g = mainPanel.graphics
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
                    .addComponent(mainPanel, GROW, GROW, GROW)
                    .addGap(8)
            )

            setVerticalGroup(
                createSequentialGroup()
                    .addGap(8)
                    .addComponent(mainPanel, GROW, GROW, GROW)
                    .addGap(8)
            )
        }
    }

    class AboutWindow : JFrame() {
        val minSz = Dimension(400, 450)

        val commonLabel: JLabel
        val pplLabel1: JLabel
        val pplLabel2: JLabel


        init {
            commonLabel = JLabel()
            commonLabel.text = "Над проектом работали : "
            pplLabel1 = JLabel()
            pplLabel1.text = "Потасьев Никита"
            pplLabel2 = JLabel()
            pplLabel2.text = "Щербанев Дмитрий"

            minimumSize = minSz

            layout = GroupLayout(contentPane).apply {
                setHorizontalGroup(
                    createSequentialGroup()
                        .addGap(8)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(commonLabel, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(16)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(pplLabel1, SHRINK, SHRINK, SHRINK)
                                .addComponent(pplLabel2, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(8)
                )

                setVerticalGroup(
                    createSequentialGroup()
                        .addGap(8)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(commonLabel, SHRINK, SHRINK, SHRINK)
                                .addComponent(pplLabel1, SHRINK, SHRINK, SHRINK)
                        )
                        .addGroup(
                            createParallelGroup()
                                .addComponent(pplLabel2, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(8)
                )
            }
        }
    }

    private fun createAboutButton(): JButton {
        val aboutButton = JButton("О программе")
        aboutButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    val frame = AboutWindow()
                    frame.isVisible = true
                    frame.defaultCloseOperation = DISPOSE_ON_CLOSE

                }
            }
        })
        return aboutButton

    }


    private fun createColorMenu(): JMenu {
        val colorMenu = JMenu("Выбор цветовой гаммы")

        val mClr = JColorChooser()
        val sClr = JColorChooser()

        var firstColor : Color
        var secondColor : Color

        firstColor = mClr.selectionModel.selectedColor
        secondColor = sClr.selectionModel.selectedColor

        colorMenu.add(mClr)
        colorMenu.add(sClr)


        return colorMenu

    }

    private fun createCtrlZButton(): JButton {
        val ctrlzButton = JButton("Отменить предыдущее действие")

        return ctrlzButton

    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        mainPanel.graphics.run {
            setXORMode(Color.WHITE)
            drawLine(-100, -100, -101, -100)
            setPaintMode()
        }
    }

    companion object {
        const val GROW = GroupLayout.DEFAULT_SIZE
        const val SHRINK = GroupLayout.PREFERRED_SIZE
    }
}