package ru.smak.video.ui.windows

import ru.smak.graphics.Plane
import ru.smak.video.objects.VideoSettings
import ru.smak.video.operations.VideoRecorderWindowOperations
import ru.smak.video.services.VideoRecorderWindowService
import java.awt.Dimension
import javax.swing.*


class VideoWindow(frame: JFrame) : JFrame() {

    private val _minSize = Dimension(260, 300);
    private val _mainPanel: JPanel = JPanel();

    var plane = Plane();

    private val _shotsListWindow = ShotsListWindow(this).apply { isVisible = false; };

    private val _service = VideoRecorderWindowService()

    val shotsCountLabel = JLabel("Shots count: ${VideoSettings.getKeyShotsCount()}")

    // spinners
    private val _widthLabel = JLabel("Width");
    val widthSpinner = JSpinner();

    private val _heightLabel = JLabel("Height");
    val heightSpinner = JSpinner();

    private val _fpsLabel = JLabel("FPS");
    val fpsSpinner = JSpinner();

    private val _durationLabel = JLabel("Duration");
    val durationSpinner = JSpinner();

    // buttons
    private val _addShotBtn = JButton("Add Shot").apply { setFocusPainted(false);setContentAreaFilled(false); };
    private val _clearBtn = JButton("Clear").apply { setFocusPainted(false);setContentAreaFilled(false); };
    private val _createBtn = JButton("Create").apply { setFocusPainted(false);setContentAreaFilled(false); };
    private val _showShotsBtn = JButton("Show Shots").apply { setFocusPainted(false);setContentAreaFilled(false); };

    // static
    companion object {
        val SHRINK = GroupLayout.PREFERRED_SIZE
        val GROW = GroupLayout.DEFAULT_SIZE
    }

    init {
        configureNames();
        setupLayout();
        setupSpinners();
        setupEventListeners();

        setLocationRelativeTo(frame);

        size = _minSize;
        isVisible = true;
    }

    private fun configureNames() {
        _addShotBtn.name = "AddShotButton";
        _clearBtn.name = "ClearShotsButton";
        _createBtn.name = "CreateVideoButton";
        _showShotsBtn.name = "ShowShotsButton";
    }


    private fun setupLayout() {

        var gl = GroupLayout(this.contentPane)
        this.layout = gl;

        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addComponent(
                    _mainPanel,
                    GROW,
                    GROW,
                    GROW
                )
        );

        gl.setHorizontalGroup(
            gl.createParallelGroup()
                .addComponent(
                    _mainPanel,
                    GROW,
                    GROW,
                    GROW
                )
        );

        gl = GroupLayout(_mainPanel);
        _mainPanel.layout = gl;

        gl.setVerticalGroup(
            gl.createParallelGroup()
                .addGap(10)
                .addGroup(
                    gl.createSequentialGroup() // resolution spinners group
                        .addGap(10)
                        .addComponent(_widthLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(widthSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_heightLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(heightSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_addShotBtn, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_createBtn, 20, SHRINK, SHRINK)
                        .addGap(10, 10, Int.MAX_VALUE)
                        .addComponent(shotsCountLabel, 20, SHRINK, SHRINK)
                )
                .addGap(30)
                .addGroup(
                    gl.createSequentialGroup()
                )
                .addGroup(
                    gl.createSequentialGroup() // video settings group
                        .addGap(10)
                        .addComponent(_fpsLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(fpsSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_durationLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(durationSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_clearBtn, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_showShotsBtn, 20, SHRINK, SHRINK)

                )


        );

        gl.setHorizontalGroup(
            gl.createSequentialGroup()
                .addGap(10)
                .addGroup(
                    gl.createParallelGroup() // resolution spinners group
                        .addGap(10)
                        .addComponent(shotsCountLabel, 20, SHRINK, SHRINK)
                        .addComponent(_widthLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(widthSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_heightLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(heightSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_addShotBtn, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_createBtn, 20, SHRINK, SHRINK)
                )
                .addGap(30)
                .addGroup(
                    gl.createParallelGroup() // video settings group
                        .addGap(10)
                        .addComponent(_fpsLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(fpsSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_durationLabel, 20, SHRINK, SHRINK)
                        .addGap(5)
                        .addComponent(durationSpinner, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_clearBtn, 20, SHRINK, SHRINK)
                        .addGap(10)
                        .addComponent(_showShotsBtn, 20, SHRINK, SHRINK)
                )
        );
    }

    private fun setupSpinners() {

        val removeSpinnerUpDownArrows = fun(spinner: JSpinner) {
            for (component in spinner.components)
                if (component.name != null && component.name.endsWith("Button"))
                    spinner.remove(component)
        }

        val widthModel = SpinnerNumberModel(800, 100, 1920, 100);
        widthSpinner.model = widthModel;
        removeSpinnerUpDownArrows(widthSpinner);

        val heightModel = SpinnerNumberModel(600, 100, 1920, 100);
        heightSpinner.model = heightModel;
        removeSpinnerUpDownArrows(heightSpinner);


        val fpsModel = SpinnerNumberModel(30, 24, 120, 5);
        fpsSpinner.model = fpsModel;
        removeSpinnerUpDownArrows(fpsSpinner);


        val durationModel = SpinnerNumberModel(5, 1, 60, 1);
        durationSpinner.model = durationModel;
        removeSpinnerUpDownArrows(durationSpinner);


    }

    private fun setupEventListeners() {
        val videoRecorderWindowOperations = VideoRecorderWindowOperations(
            this,
            _shotsListWindow,
            _service
        )

        _addShotBtn.addMouseListener(videoRecorderWindowOperations)
        _clearBtn.addMouseListener(videoRecorderWindowOperations)
        _showShotsBtn.addMouseListener(videoRecorderWindowOperations)
        _createBtn.addMouseListener(videoRecorderWindowOperations)
    }


}