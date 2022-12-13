package ru.smak.gui

import ru.smak.graphics.*
import ru.smak.math.Complex
import ru.smak.math.Julia
import ru.smak.math.Mandelbrot
import ru.smak.video.ui.windows.VideoWindow
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.*
import kotlin.random.Random


open class MainWindow : JFrame() {
    private class Rollback(private val plane: Plane,
                           private val targetSz: TargetSz,
                           private val dimension: Dimension) {
        private val xMin = targetSz.targetXMin
        private val xMax = targetSz.targetXMax
        private val yMin = targetSz.targetYMin
        private val yMax = targetSz.targetYMax
        fun rollback() {
            makeOneToOne(plane, xMin, xMax, yMin, yMax, dimension, targetSz)
        }
    }
    private val operations = mutableListOf<Rollback>()
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(1000, 600)
    val mainPanel: GraphicsPanel

    private val _videoWindow = VideoWindow(this).apply { isVisible = false; };






    val trgsz = TargetSz()
    private var startPoint: Point? = null
    private var numButtonPressed: Int = 0

    var firstColor: Color = Color.BLACK
    var secondColor: Color = Color.WHITE

    init {
        val menuBar = JMenuBar().apply {
            add(createOpenButton())
            add(createSaveButton())
            add(createColorMenu())
            add(createDynamicalItsButton())
            add(createCtrlZButton())
            add(createAboutButton())
        }

        jMenuBar = menuBar

        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = minSz

        colorScheme = ColorFuncs[Random.nextInt(ColorFuncs.size)]

        val plane = Plane(-2.0, 1.0, -1.0, 1.0)
        trgsz.getTargetFromPlane(plane)
        val fp = FractalPainter(Mandelbrot()::isInSet, colorScheme, plane)
        //val fpj = FractalPainter(Julia()::isInSet, ::testFunc, plane)
        mainPanel = GraphicsPanel().apply {
            background = Color.WHITE
            addPainter(fp)
            //addPainter(fpj)

        }

        mainPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                plane.width=mainPanel.width
                plane.height=mainPanel.height
                makeOneToOne(plane,trgsz, mainPanel.size)//Делает панель мастштабом 1 к 1
            }
        })


    menuBar.add(createRecordBtn(plane)); // создаем окошко для создания видео


    mainPanel.addMouseListener(
    object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)
                e?.let {
                    if (it.button == MouseEvent.BUTTON1) {
                        SecondWindow(colorScheme).apply {
                            Julia.selectedPoint =
                                Complex(Converter.xScrToCrt(it.x, plane), Converter.yScrToCrt(it.y, plane))
                            isVisible = true
                        }
                    }
                }
            }
        })

        mainPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    if (it.button == MouseEvent.BUTTON1)
                        rect.addPoint(it.point)
                    else if (it.button == MouseEvent.BUTTON3) {
                        startPoint = it.point
                    }
                    operations.add(Rollback(plane, trgsz, mainPanel.size))
                    numButtonPressed = it.button
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                if (numButtonPressed == MouseEvent.BUTTON1) {
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
                            makeOneToOne(
                                plane,
                                x1,
                                x2,
                                y1,
                                y2,
                                mainPanel.size,
                                trgsz
                            )//Делает панель мастштабом 1 к 1 и меняет trgs
                            mainPanel.repaint()
                        }
                    }
                    rect.destroy()
                } else if (numButtonPressed == MouseEvent.BUTTON3) {
                    startPoint = null
                }
                numButtonPressed = 0
            }
        })

        mainPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                if (numButtonPressed == MouseEvent.BUTTON1) {
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
                } else if (numButtonPressed == MouseEvent.BUTTON3) {
                    if (e != null) {
                        startPoint?.let {
                            val shiftX = Converter.xScrToCrt(e.x, plane) - Converter.xScrToCrt(it.x, plane)
                            val shiftY = Converter.yScrToCrt(e.y, plane) - Converter.yScrToCrt(it.y, plane)
                            trgsz.shiftImage(shiftX, shiftY, plane)
                            makeOneToOne(plane, trgsz, mainPanel.size)
                            startPoint = e.point
                            mainPanel.repaint()
                        }
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

    class Text_Animation : JPanel() {
        var k = 0
        var l = 100

        override fun paint(gp: Graphics) {
            super.paint(gp)
            val g2d = gp as Graphics2D

            val file = File("Font.ttf")
            val font = Font.createFont(Font.TRUETYPE_FONT, file)
            val sFont = font.deriveFont(25f)
            g2d.color = Color.RED
            g2d.font = sFont

            val pplArray = listOf<String>(
                "Потасьев Никита", "Щербанев Дмитрий",
                "Балакин Александр", "Иванов Владислав",
                "Хусаинов Данил", "Даянов Рамиль", "Королева Ульяна",
                "Цымбал Данила"
            )

            pplArray.forEachIndexed { i, s -> g2d.drawString(s, k + i * 20, l + i * 30) }
            g2d.drawString("Над проектом работали", width / 4, 50)

            try {
                Thread.sleep(200)
                k += 20
                if (k > width) {
                    k = 0
                }
                repaint()

            } catch (ex: InterruptedException) {
                JOptionPane.showMessageDialog(this, ex)
            }
        }
    }

    class AboutWindow : JFrame() {
        val minSz = Dimension(400, 450)

        val commonLabel: JLabel
        var pplLabel = JTextArea()


        init {
            commonLabel = JLabel()
            commonLabel.text = "Над проектом работали : "
            pplLabel.isEnabled = false
            pplLabel.text = "Потасьев Никита \n" +
                    "Щербанев Дмитрий \n" +
                    "Балакин Александр \n" +
                    "Иванов Владислав \n" +
                    "Хусаинов Данил \n" +
                    "Даянов Рамиль \n" +
                    "Королева Ульяна \n" +
                    "Цымбал Данила"


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
                                .addComponent(pplLabel, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(8)
                )

                setVerticalGroup(
                    createSequentialGroup()
                        .addGap(8)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(commonLabel, SHRINK, SHRINK, SHRINK)
                                .addComponent(pplLabel, SHRINK, SHRINK, SHRINK)
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
                    val frame = JFrame()
                    frame.minimumSize = Dimension(1200, 450)
                    frame.add(Text_Animation())
                    frame.isVisible = true
                    frame.defaultCloseOperation = DISPOSE_ON_CLOSE

                }
            }
        })
        return aboutButton

    }


    private fun createColorMenu(): JMenu {
        val colorMenu = JMenu("Выбор цветовой гаммы")

        val fClr = JColorChooser()
        val sClr = JColorChooser()

        fClr.addPropertyChangeListener {
            firstColor = fClr.selectionModel.selectedColor
            repaint()
        }

        sClr.addPropertyChangeListener {
            secondColor = sClr.selectionModel.selectedColor
            repaint()
        }

        colorMenu.add(fClr)
        colorMenu.add(sClr)

        return colorMenu
    }

    private fun createOpenButton(): JButton {
        val openButton = JButton("Открыть")
        var fileChooser = JFileChooser()
        openButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                fileChooser.dialogTitle = "Выбор директории"
                fileChooser.showOpenDialog(this@MainWindow)
            }
        })
        return openButton
    }

    private fun createSaveButton(): JButton {
        val saveButton = JButton("Сохранить")
        return saveButton
    }

    private fun createDynamicalItsButton(): JButton {
        val dynIt = JButton("Динамическая итерация")
        return dynIt
    }

    private fun createCtrlZButton(): JButton {
        val ctrlzButton = JButton("Отменить предыдущее действие")
        ctrlzButton.addMouseListener(
            object : MouseAdapter(){
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    if (operations.size > 0) {
                        operations.last().rollback()
                        operations.removeAt(operations.lastIndex)
                        mainPanel.repaint()
                    }
                }
            }
        )

        return ctrlzButton

    }

private fun createRecordBtn(plane: Plane): JButton {
    val btn = JButton("Record");

    btn.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
            super.mousePressed(e)
            e?.let {
                _videoWindow.apply {
                    this.plane = plane;
                    isVisible = true
                }
            }
        }
    })
    return btn;
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

        var colorScheme: (Float) -> Color = ::testFunc;
    }

// TODO: for testing video creation
fun getScreenShot(width: Int, height: Int): BufferedImage {

    val image = BufferedImage(
        width,
        height,
        BufferedImage.TYPE_INT_RGB
    )
    mainPanel.paint(image.graphics)
    return image
}



}