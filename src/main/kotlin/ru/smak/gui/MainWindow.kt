package ru.smak.gui

import ru.smak.graphics.*
import ru.smak.math.*
import ru.smak.tools.FractalData
import ru.smak.tools.FractalDataFileLoader
import ru.smak.tools.FractalDataFileSaver
import ru.smak.video.ui.windows.VideoWindow
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.abs
import kotlin.random.Random


open class MainWindow : JFrame() {
    private var plane: Plane
    private var fp: FractalPainter
    var image = BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)

        private class Rollback(
        private val plane: Plane,
        private val targetSz: TargetSz,
        private val dimension: Dimension
    ) {
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

    private val _videoWindow = VideoWindow(this).apply { isVisible = false; }


    val trgsz = TargetSz()
    private var startPoint: Point? = null
    private var numButtonPressed: Int = 0
    var checkbox= createDynamicalItsButton()

    init {
        val menuBar = JMenuBar().apply {
            add(createFileMenu())
            add(createColorMenu())
            add(checkbox)
            add(createCtrlZButton())
            add(createAboutButton())
        }

        jMenuBar = menuBar

        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = minSz


        plane = Plane(-2.0, 1.0, -1.0, 1.0)

        trgsz.getTargetFromPlane(plane)
        fp = FractalPainter(fractalScheme, colorScheme, plane)

        mainPanel = GraphicsPanel().apply {
            background = Color.WHITE
            addPainter(fp)
        }

        mainPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                plane.width=mainPanel.width
                plane.height=mainPanel.height
                makeOneToOne(plane,trgsz, mainPanel.size)//Делает панель мастштабом 1 к 1
            }
        })

        menuBar.add(createRecordBtn(plane)) // создаем окошко для создания видео


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
                            if (checkbox.isSelected){
                            val sq: Int = plane.height * plane.width
                            val new_sq = abs(x2-x1) * abs(y2-y1)
                            var d: Int = 100
                            if(sq/new_sq<100) d = (sq/new_sq).toInt()
                            Mandelbrot.maxIterations += d}
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

    class AboutPanel : JPanel() {
        private var k = 0
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
                "Цымбал Данила", "Нигматов Аяз", "Домашев Данил",
                "Шилин Юрий Эдуардович", "Трепачко Данила",
                "Алуна Фис"
            )

            pplArray.forEachIndexed { i, s -> g2d.drawString(s, k + i * 20, l + i * 30) }
            g2d.drawString("Над проектом работали", width / 4, 50).apply { CENTER_ALIGNMENT }

            try {
                Thread.sleep(8)
                k += 1
                if (k > width) {
                    k = 0
                }
                repaint()

            } catch (ex: InterruptedException) {
                JOptionPane.showMessageDialog(this, ex)
            }
        }
    }

    private fun createFileMenu(): JMenu {
        val openItem = JMenuItem("Открыть")
        openItem.addActionListener {
            val fractalData = FractalDataFileLoader.loadData()
            if (fractalData != null) {
                plane.xEdges = Pair(fractalData.xMin, fractalData.xMax)
                plane.yEdges = Pair(fractalData.yMin, fractalData.yMax)
                fp.plane.xEdges = Pair(fractalData.xMin, fractalData.xMax)
                fp.plane.yEdges = Pair(fractalData.yMin, fractalData.yMax)
                fp.colorFunc = ColorFuncs[fractalData.colorFuncIndex]
                colorScheme = ColorFuncs[fractalData.colorFuncIndex]
                checkbox.isSelected = fractalData.isDynamical
                trgsz.getTargetFromPlane(plane)
                this.repaint()
            }
        }
        
        val selfFormatMenuItem = JMenuItem("Фрактал")
        selfFormatMenuItem.addActionListener {
            val fractalData = FractalData(plane.xMin, plane.xMax, plane.yMin, plane.yMax, colorFuncIndex, checkbox.isSelected)
            val fractalSaver = FractalDataFileSaver(fractalData)
        }
        
        val saveImageMenuItem = JMenuItem("Изображение")
        val fileChooser = JFileChooser()
        saveImageMenuItem.addMouseListener(object : MouseAdapter() {
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
        
        val saveMenu = JMenu("Сохранить как")
        saveMenu.add(selfFormatMenuItem)
        saveMenu.addSeparator()
        saveMenu.add(saveImageMenuItem)

        val fileMenu = JMenu("Файл")
        fileMenu.add(openItem)
        fileMenu.addSeparator()
        fileMenu.add(saveMenu)

        return fileMenu
    }

    private fun createAboutButton(): JButton {
        val aboutButton = JButton("О программе")
        aboutButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    val frame = JFrame()
                    frame.isVisible = true
                    frame.add(AboutPanel())
                    frame.minimumSize = Dimension(800, 500)
                    frame.pack()
                    frame.defaultCloseOperation = DISPOSE_ON_CLOSE

                }
            }
        })
        return aboutButton

    }

    private fun createSaveButtonImage(plane: Plane): JButton{
        val btnSave = JButton("Save")
        btnSave.addActionListener{
            val img = BufferedImage(mainPanel.width,mainPanel.height+infoHeight,BufferedImage.TYPE_INT_RGB)
            preparImg(img,mainPanel,plane)
            SaveImage(img).actionPerformed(null)
        }
        btnSave.isVisible = true
        return btnSave
    }



    private fun createColorMenu(): JMenu {
        val colorMenu = JMenu("Выбор цветовой гаммы")

        val colorSchema1 = JButton()
        colorSchema1.text = "Цветовая схема #1"
        colorSchema1.addActionListener { colorFuncIndex = 0
            fp.colorFunc = ColorFuncs[colorFuncIndex]
            colorScheme = ColorFuncs[colorFuncIndex]
            mainPanel.repaint()
            }
        val colorSchema2 = JButton()
        colorSchema2.text = "Цветовая схема #2"
        colorSchema2.addActionListener { colorFuncIndex = 1
            fp.colorFunc = ColorFuncs[colorFuncIndex]
            colorScheme = ColorFuncs[colorFuncIndex]
            mainPanel.repaint()}
        val colorSchema3 = JButton()
        colorSchema3.text = "Цветовая схема #3"
        colorSchema3.addActionListener { colorFuncIndex = 2
            fp.colorFunc = ColorFuncs[colorFuncIndex]
            colorScheme = ColorFuncs[colorFuncIndex]
            mainPanel.repaint()}


        colorMenu.add(colorSchema1)
        colorMenu.add(colorSchema2)
        colorMenu.add(colorSchema3)

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
        dynIt.isSelected=true
        return dynIt
    }

    private fun createCtrlZButton(): JButton {
        val ctrlzButton = JButton("Отменить предыдущее действие")
        ctrlzButton.addMouseListener(
            object : MouseAdapter() {
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
        val btn = JButton("Record")

        btn.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    _videoWindow.apply {
                        this.plane = plane
                        isVisible = true
                    }
                }
            }
        })
        return btn
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

        var colorFuncIndex: Int = 0
        var FractalFuncIndex: Int = 0
        var colorScheme = ColorFuncs[colorFuncIndex]
        var fractalScheme = FractalFuncs[FractalFuncIndex]
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