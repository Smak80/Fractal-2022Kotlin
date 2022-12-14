package ru.smak.video.ui.windows

import ru.smak.video.services.imageCreated
import ru.smak.video.services.imageCreatingFinished
import ru.smak.video.services.videoRecordingFinished
import ru.smak.video.services.videoRecordingProcessing
import java.awt.Dimension
import javax.swing.GroupLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextField
import kotlin.math.min

class ProgressWindow(
    parent: JFrame,
    private val _totalFramesCount: Int
) : JFrame() {

    var progressBar = JProgressBar(0, _totalFramesCount)

    private val _mainPanel = JPanel();
    private val _processTextBar = JTextField("Process name");

    private var _progress = 0

    companion object { // todo: abstract class Window with these objects to inherit?
        val SHRINK = GroupLayout.PREFERRED_SIZE;
        val GROW = GroupLayout.DEFAULT_SIZE;
    }

    init {
        size = Dimension(400,150);
        isResizable = false;
        isAlwaysOnTop = true;
        defaultCloseOperation = DISPOSE_ON_CLOSE;
        setLocationRelativeTo(parent);

        _processTextBar.apply {
            minimumSize = Dimension(300, 30)
            preferredSize = Dimension(300, 30)
            maximumSize = Dimension(300, 30)
            text = "Frames creating..."
            isEditable = false
        }

        setupLayout();
        setupEventListeners();
    }

    private fun setupEventListeners() {
        imageCreated.addListener { _, number ->
            _progress += 1
            progressBar.value = _progress
            _processTextBar.text = "Frames creating... $_progress / $_totalFramesCount"
            println("Image created: $number / $_totalFramesCount")
        }

        imageCreatingFinished.addListener { _, _ ->
            progressBar.value = _totalFramesCount
            _progress = 0
            _processTextBar.text = "Frames creating finished!"
            println("Image creating finished")
        }

        videoRecordingProcessing.addListener {_,_ ->
            _progress += 1
            _processTextBar.text = "Video rendering... $_progress / $_totalFramesCount"
            progressBar.value = _progress
        }

        videoRecordingFinished.addListener {_, time ->
            _processTextBar.text = "Video creating finished, time: $time"
            println("Video creating finished, time: $time")
        }
    }

    private fun setupLayout()
    {
        _mainPanel.layout = GroupLayout(_mainPanel).apply {
            setVerticalGroup(
                createSequentialGroup()
                    .addGap(10)
                    .addComponent(progressBar, 10, SHRINK, SHRINK)
                    .addGap(30)
                    .addComponent(_processTextBar, GROW, GROW, GROW)
                    .addGap(10)
            )

            setHorizontalGroup(
                createParallelGroup()
                    .addGap(10)
                    .addComponent(progressBar, SHRINK, GROW, GROW)
                    .addGap(70)
                    .addComponent(_processTextBar, GROW, GROW, GROW)
            )
        }
        add(_mainPanel);
    }
}