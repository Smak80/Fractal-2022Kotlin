package ru.smak.video.ui.windows

import ru.smak.video.objects.VideoSettings
import ru.smak.video.operations.ProgressWindowOperations
import ru.smak.video.operations.VideoRecorderWindowOperations
import java.awt.Color
import java.awt.Dimension
import javax.swing.GroupLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JProgressBar

class ProgressWindow(parent: JFrame) : JFrame() {

    val progressBar = JProgressBar(0,VideoSettings.requiredShotsCount);

    private val _mainPanel = JPanel();
    private val _cancelBtn = JButton("Cancel").apply { name = "CancelButton" };

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

        setupLayout();
        setupEventListeners();
    }

    private fun setupEventListeners() {
        val operations = ProgressWindowOperations();

        _cancelBtn.addMouseListener(operations);
    }

    private fun setupLayout()
    {
        _mainPanel.layout = GroupLayout(_mainPanel).apply {
            setVerticalGroup(
                createSequentialGroup()
                    .addGap(10)
                    .addComponent(progressBar, 10, SHRINK, SHRINK)
                    .addGap(20)
                    .addComponent(_cancelBtn,20, SHRINK, SHRINK)
                    .addGap(10)
            )

            setHorizontalGroup(
                createParallelGroup()
                    .addGap(10)
                    .addComponent(progressBar, SHRINK, GROW, GROW)
                    .addComponent(_cancelBtn,20, SHRINK, SHRINK)
            )
        }
        add(_mainPanel);
    }



}