package ru.smak.gui

import ru.smak.graphics.ColorFuncs
import ru.smak.graphics.Converter
import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.math.Complex
import ru.smak.math.Julia
import ru.smak.math.Mandelbrot
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.media.bean.playerbean.MediaPlayer
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.random.Random


open class MainWindow : JFrame() {
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(1000, 450)
    val mainPanel: GraphicsPanel
    val trgsz = TargetSz()
    private var startPoint: Point? = null
    private var numButtonPressed: Int = 0

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

        val colorScheme = ColorFuncs[Random.nextInt(ColorFuncs.size)]
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
                plane.width = mainPanel.width
                plane.height = mainPanel.height
                makeOneToOne(plane, trgsz, mainPanel.size)//Делает панель мастштабом 1 к 1
            }
        })

        mainPanel.addMouseListener(object : MouseAdapter() {

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
                    else if (it.button == MouseEvent.BUTTON3)
                        startPoint = it.point
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
                            )//Делает панель мастштабом 1 к 1 и меняет trgsz
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

    internal class Video : JFrame() {
        var player //наш плеер
                : MediaPlayer

        init {
            defaultCloseOperation = EXIT_ON_CLOSE
            size = Dimension(640, 480) //устанавливаем размер окна
            player = MediaPlayer()
            val path = "video.mp4"
            //path - путь к файлу
            player.mediaLocation = "file:///$path"
            player.playbackLoop = false //Повтор видео
            player.prefetch() //предварительная обработка плеера (без неё плеер не появится)
            //добавляем на фрейм
            add(player)
            //player.start (); - сразу запустить плеер
            isVisible = true
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
                    "Королева Ульяна"


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
                    frame.add(Video())
                    frame.isVisible = true
                    frame.defaultCloseOperation = DISPOSE_ON_CLOSE
                }
            }
        })
        return aboutButton

    }

    private fun createColorMenu(): JMenu {
        val colorMenu = JMenu("Выбор цветовой гаммы")

        val colorSchema1 = JButton()
        colorSchema1.text = "Цветовая схема #1"
        val colorSchema2 = JButton()
        colorSchema2.text = "Цветовая схема #2"
        val colorSchema3 = JButton()
        colorSchema3.text = "Цветовая схема #3"
        val colorSchema4 = JButton()
        colorSchema4.text = "Цветовая схема #4"
        val colorSchema5 = JButton()
        colorSchema5.text = "Цветовая схема #5"

        colorMenu.add(colorSchema1)
        colorMenu.add(colorSchema2)
        colorMenu.add(colorSchema3)
        colorMenu.add(colorSchema4)
        colorMenu.add(colorSchema5)



        return colorMenu
    }

    private fun createOpenButton(): JButton {
        val openButton = JButton("Открыть")
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter(
            "image", "JPG", ".PNG"
        )
        fileChooser.fileFilter = filter
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
        val fileChooser = JFileChooser()
        saveButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                fileChooser.dialogTitle = "Сохранение файла"
                val result = fileChooser.showSaveDialog(this@MainWindow)
                if (result == JFileChooser.APPROVE_OPTION) JOptionPane.showMessageDialog(
                    this@MainWindow,
                    "Файл '" + fileChooser.selectedFile +
                            "  сохранен, наверное"
                )
            }
        })
        return saveButton
    }

    private fun createDynamicalItsButton(): JCheckBox {
        val dynIt = JCheckBox("Динамическая итерация")
        return dynIt
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