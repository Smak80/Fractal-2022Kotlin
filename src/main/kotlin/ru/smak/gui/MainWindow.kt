package ru.smak.gui

import ru.smak.graphics.ColorFuncs
import ru.smak.graphics.Converter
import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.math.Complex
import ru.smak.math.FractalFuncs
import ru.smak.math.Julia
import ru.smak.math.Mandelbrot
import ru.smak.tools.FractalData
import ru.smak.tools.FractalDataFileLoader
import ru.smak.tools.FractalDataFileSaver
import ru.smak.video.ui.windows.VideoWindow
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.abs


open class MainWindow : JFrame() {
    private var plane: Plane
    private var fp: FractalPainter
    var image = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    private inner class Rollback(
        private val targetSz: TargetSz,
    ) {
        private val xMin = targetSz.targetXMin
        private val xMax = targetSz.targetXMax
        private val yMin = targetSz.targetYMin
        private val yMax = targetSz.targetYMax
        private val maxIterations = Mandelbrot.maxIterations
        fun rollback() {
            makeOneToOne(plane, xMin, xMax, yMin, yMax, mainPanel.size, targetSz)
            Mandelbrot.maxIterations = maxIterations
        }
    }

    private val operations = mutableListOf<Rollback>()
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(1000, 600)
    val mainPanel: GraphicsPanel
    var sw: SecondWindow? = null

    private lateinit var dynIt: JCheckBox

    private val _videoWindow = VideoWindow(this).apply { isVisible = false; }


    val trgsz = TargetSz()
    private var startPoint: Point? = null
    private var numButtonPressed: Int = 0
    var checkbox = createDynamicIt()

    val startedArea:Double

    init {
        plane = Plane(-2.0, 1.0, -1.0, 1.0)
        startedArea = (plane.xMax-plane.xMin) * (plane.yMax-plane.yMin)
        val videoMenu = createVideoMenu()

        val menuBar = JMenuBar().apply {
            add(createFileMenu())
            add(createFractalActionMenu())
            add(createAboutButton())
            add(videoMenu)

        }

        jMenuBar = menuBar

        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = minSz




        trgsz.getTargetFromPlane(plane)
        fp = FractalPainter(fractalScheme, colorScheme, plane)

        mainPanel = GraphicsPanel().apply {
            background = Color.WHITE
            addPainter(fp)
        }

        mainPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                plane.width = mainPanel.width
                plane.height = mainPanel.height
                makeOneToOne(plane, trgsz, mainPanel.size)//Делает панель мастштабом 1 к 1
            }
        })

        videoMenu.add(createRecordBtn(plane)) // создаем окошко для создания видео


        mainPanel.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    e?.let {
                        if (it.button == MouseEvent.BUTTON1 && fractalScheme == FractalFuncs[0]) {
                            sw?.let {
                                if (isEnabled) it.dispose()
                            }
                            sw = SecondWindow(colorScheme).apply {
                                Julia.selectedPoint =
                                    Complex(Converter.xScrToCrt(it.x, plane), Converter.yScrToCrt(it.y, plane))
                                isVisible = true
                            }
                        }
                    }
                }
            })


        val pressed: Action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                if (operations.size > 0) {
                    operations.last().rollback()
                    operations.removeAt(operations.lastIndex)
                    mainPanel.repaint()
                }
            }
        }

        menuBar.inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
            "pressed"
        )

        menuBar.actionMap.put(
            "pressed",
            pressed
        )



        mainPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    if (it.button == MouseEvent.BUTTON1)
                        rect.addPoint(it.point)
                    else if (it.button == MouseEvent.BUTTON3) {
                        startPoint = it.point
                    }
                    operations.add(Rollback(trgsz))
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
                            if (dynIt.isSelected) {
                                val new_sq = abs(x2 - x1) * abs(y2 - y1)
                                var d = (1800.0*(1.0-Math.sqrt(Math.sqrt(Math.sqrt(new_sq /(startedArea)))))).toInt()
                                if (dynIt.isSelected==true) {
                                    Mandelbrot.maxIterations =200+d
                                    println(Mandelbrot.maxIterations)
                                }

                            }
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
                            //trgsz.shiftImageOnPanel(shiftX, shiftY, plane)
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

            val pplArray = listOf(
                "Потасьев Никита", "Щербанев Дмитрий",
                "Балакин Александр", "Иванов Владислав",
                "Хусаинов Данил", "Даянов Рамиль", "Королева Ульяна",
                "Цымбал Данила", "Нигматов Аяз", "Домашев Данил",
                "Шилин Юрий Эдуардович", "Трепачко Данила",
                "Алуна Фис"
            )

            pplArray.forEachIndexed { i, s -> g2d.drawString(s, k + i * 20, l + i * 30) }
            g2d.drawString("Над проектом Fractal работали", width / 4, 50).apply { CENTER_ALIGNMENT }

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
        val openItem = JMenuItem("Открыть...")
        openItem.addActionListener {
            val fractalData = FractalDataFileLoader.loadData()
            if (fractalData != null) {
                plane.xEdges = Pair(fractalData.xMin, fractalData.xMax)
                plane.yEdges = Pair(fractalData.yMin, fractalData.yMax)
                fp.plane.xEdges = Pair(fractalData.xMin, fractalData.xMax)
                fp.plane.yEdges = Pair(fractalData.yMin, fractalData.yMax)
                colorFuncIndex = fractalData.colorFuncIndex
                _colorSchemes[colorFuncIndex].doClick()
                if (!_colorSchemes[colorFuncIndex].isSelected) {
                    _colorSchemes[colorFuncIndex].isSelected = true
                }
                FractalFuncIndex = fractalData.fractalFuncIndex
                fractalScheme = FractalFuncs[FractalFuncIndex]
                dynIt.isSelected = fractalData.isDynamical
                fp.colorFunc = ColorFuncs[fractalData.colorFuncIndex]
                colorScheme = ColorFuncs[fractalData.colorFuncIndex]
                checkbox.isSelected = fractalData.isDynamical
                trgsz.getTargetFromPlane(plane)
                Mandelbrot.maxIterations = fractalData.maxIterations
                _fractalSchemes[fractalData.fractalFuncIndex].doClick()
                if (!_fractalSchemes[fractalData.fractalFuncIndex].isSelected) {
                    _fractalSchemes[fractalData.fractalFuncIndex].isSelected = true
                }
                this.repaint()
            }
        }

        val selfFormatMenuItem = JMenuItem("Фрактал...")
        selfFormatMenuItem.addActionListener {
            val fractalData = FractalData(
                plane.xMin,
                plane.xMax,
                plane.yMin,
                plane.yMax,
                FractalFuncIndex,
                colorFuncIndex,
                dynIt.isSelected,
                Mandelbrot.maxIterations
            )
            val fractalSaver = FractalDataFileSaver(fractalData)
        }

        val saveImageMenuItem = JMenuItem("Изображение...")
        val fileChooser = JFileChooser()
        saveImageMenuItem.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                fileChooser.dialogTitle = "Сохранение файла..."
                val result = fileChooser.showSaveDialog(this@MainWindow)
                if (result == JFileChooser.APPROVE_OPTION) JOptionPane.showMessageDialog(
                    this@MainWindow,
                    "Файл '" + fileChooser.selectedFile +
                            "  сохранен, наверное"
                )
            }
        })

        val saveMenu = JMenu("Сохранить как...")
        saveMenu.add(selfFormatMenuItem)
        saveMenu.addSeparator()
        saveMenu.add(createSaveButtonImage())

        val fileMenu = JMenu("Файл")
        fileMenu.add(openItem)
        fileMenu.addSeparator()
        fileMenu.add(saveMenu)

        return fileMenu
    }

    private fun createAboutButton(): JMenu {
        val aboutMenu = JMenu("О программе")
        val frame = JFrame()
        frame.add(AboutPanel())
        frame.minimumSize = Dimension(800, 500)
        frame.pack()
        frame.defaultCloseOperation = DISPOSE_ON_CLOSE
        aboutMenu.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    if (frame.isShowing) {
                        frame.dispose()
                    }
                    frame.isVisible = true
                }
            }
        })
        return aboutMenu
    }


    private fun createSaveButtonImage(): JMenuItem {
        val btnSave = JMenuItem("Изображение...")
        btnSave.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                val img = BufferedImage(mainPanel.width, mainPanel.height + infoHeight, BufferedImage.TYPE_INT_RGB)
                preparImg(img, mainPanel, plane)
                SaveImage(img).actionPerformed(null)
            }
        })
        btnSave.isVisible = true
        return btnSave
    }
    
    private lateinit var _fractalSchemes: Array<JRadioButton>

    private fun createFractalMenu(): JMenu {
        val fractalMenu = JMenu("Выбор фрактала")

        val fractalSchema1 = JRadioButton()
        fractalSchema1.text = "Мандельброт"
        fractalSchema1.isSelected = true

        val fractalSchema2 = JRadioButton()
        fractalSchema2.text = "Жюлиа"

        val fractalSchema3 = JRadioButton()
        fractalSchema3.text = "Рандомный фрактал"

        fractalSchema1.addActionListener {
            FractalFuncIndex = 0
            fp.fractal = FractalFuncs[FractalFuncIndex]
            fractalScheme = FractalFuncs[FractalFuncIndex]
            mainPanel.repaint()
            fractalSchema2.isSelected = false
            fractalSchema3.isSelected = false
        }

        fractalSchema2.addActionListener {
            FractalFuncIndex = 1
            fp.fractal = FractalFuncs[FractalFuncIndex]
            fractalScheme = FractalFuncs[FractalFuncIndex]
            mainPanel.repaint()
            fractalSchema1.isSelected = false
            fractalSchema3.isSelected = false
        }

        fractalSchema3.addActionListener {
            FractalFuncIndex = 2
            fp.fractal = FractalFuncs[FractalFuncIndex]
            fractalScheme = FractalFuncs[FractalFuncIndex]
            mainPanel.repaint()
            fractalSchema1.isSelected = false
            fractalSchema2.isSelected = false
        }
        
        _fractalSchemes = arrayOf(fractalSchema1, fractalSchema2, fractalSchema3)

        fractalMenu.add(fractalSchema1)
        fractalMenu.add(fractalSchema2)
        fractalMenu.add(fractalSchema3)

        return fractalMenu
    }

    private lateinit var _colorSchemes: Array<JRadioButton>

    private fun createColorMenu(): JMenu {
        val colorMenu = JMenu("Выбор цветовой гаммы")

        val colorSchema1 = JRadioButton()
        colorSchema1.text = "Цветовая схема #1"
        colorSchema1.isSelected = true

        val colorSchema2 = JRadioButton()
        colorSchema2.text = "Цветовая схема #2"

        val colorSchema3 = JRadioButton()
        colorSchema3.text = "Цветовая схема #3"

        _colorSchemes = Array(3, init = { _ -> JRadioButton() })

        _colorSchemes[0] = colorSchema1
        _colorSchemes[1] = colorSchema2
        _colorSchemes[2] = colorSchema3

        colorSchema1.addActionListener {
            colorFuncIndex = 0
            fp.colorFunc = ColorFuncs[colorFuncIndex]
            colorScheme = ColorFuncs[colorFuncIndex]
            mainPanel.repaint()
            colorSchema3.isSelected = false
            colorSchema2.isSelected = false
            sw?.let {
                if (isEnabled) {
                    it.changeColor(colorScheme)
                }
            }
        }

        colorSchema2.addActionListener {
            colorFuncIndex = 1
            fp.colorFunc = ColorFuncs[colorFuncIndex]
            colorScheme = ColorFuncs[colorFuncIndex]
            mainPanel.repaint()
            colorSchema1.isSelected = false
            colorSchema3.isSelected = false
            sw?.let {
                if (isEnabled) {
                    it.changeColor(colorScheme)
                }
            }
        }

        colorSchema3.addActionListener {
            colorFuncIndex = 2
            fp.colorFunc = ColorFuncs[colorFuncIndex]
            colorScheme = ColorFuncs[colorFuncIndex]
            mainPanel.repaint()
            colorSchema1.isSelected = false
            colorSchema2.isSelected = false
            sw?.let {
                if (isEnabled) {
                    it.changeColor(colorScheme)
                }
            }
        }

        colorMenu.add(colorSchema1)
        colorMenu.add(colorSchema2)
        colorMenu.add(colorSchema3)

        return colorMenu
    }

    private fun createOpenButton(): JMenu {
        val openButton = JMenu("Открыть...")
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

    private fun createSaveButton(): JMenu {
        val saveButton = JMenu("Сохранить")
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

    private fun createDynamicIt(): JMenu {
        val dynMenu = JMenu("Переключатель динамической итерации")
        dynIt = JCheckBox("Динамическая итерация")
        dynIt.isSelected = true

        dynMenu.add(dynIt)
        return dynMenu
    }



    private fun createCtrlZButton(): JMenuItem {
        val ctrlZMenu = JMenuItem("Отменить предыдущее действие")

        val pressed: Action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                if (operations.size > 0) {
                    operations.last().rollback()
                    operations.removeAt(operations.lastIndex)
                    mainPanel.repaint()
                }
            }
        }


        ctrlZMenu.inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
            "pressed"
        )

        ctrlZMenu.actionMap.put(
            "pressed",
            pressed
        )

        ctrlZMenu.addActionListener(){
            if (operations.size > 0) {
                operations.last().rollback()
                operations.removeAt(operations.lastIndex)
                mainPanel.repaint()
            }
        }

        return ctrlZMenu

    }

    private fun createRecordBtn(plane: Plane): JMenuItem {
        val btn = JMenuItem("Record...")

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

    private fun createFormulaMenu(): JMenu {
        val colorMenu = JMenu("Выбор формулы")

        val formula1 = JRadioButton()
        formula1.text = "Формула #1"
        formula1.isSelected = true

        val formula2 = JRadioButton()
        formula2.text = "Формула #2"

        val formula3 = JRadioButton()
        formula3.text = "Формула #3"

        formula1.addActionListener {

            mainPanel.repaint()
            formula3.isSelected = false
            formula2.isSelected = false
        }

        formula2.addActionListener {

            mainPanel.repaint()
            formula1.isSelected = false
            formula3.isSelected = false
        }

        formula3.addActionListener {

            mainPanel.repaint()
            formula1.isSelected = false
            formula2.isSelected = false
        }

        colorMenu.add(formula1)
        colorMenu.add(formula2)
        colorMenu.add(formula3)

        return colorMenu
    }

    private fun createFractalActionMenu(): JMenu {
        val frActMenu = JMenu("Действия над фракталом")
        frActMenu.add(createColorMenu())
        frActMenu.addSeparator()
        frActMenu.add(createFractalMenu())
        frActMenu.addSeparator()
        frActMenu.add(createDynamicIt())
        frActMenu.addSeparator()
        frActMenu.add(createCtrlZButton())
        return frActMenu
    }

    private fun createVideoMenu(): JMenu {
        val videoMenu = JMenu("Запись видео")
        return videoMenu
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