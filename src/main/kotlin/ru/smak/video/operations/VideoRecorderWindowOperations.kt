package ru.smak.video.operations

import ru.smak.graphics.Plane
import ru.smak.video.entities.Shot
import ru.smak.video.objects.VideoSettings
import ru.smak.video.models.CreateVideoModel
import ru.smak.video.services.VideoRecorderWindowService
import ru.smak.video.ui.windows.ProgressWindow
import ru.smak.video.ui.windows.ShotsListWindow
import ru.smak.video.ui.windows.VideoWindow
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JFileChooser

class VideoRecorderWindowOperations(
    private val _mainWindow: VideoWindow,
    private val _shotsListWindow: ShotsListWindow,
    private val _progressWindow: ProgressWindow,
    private val _service: VideoRecorderWindowService
) : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent?) {

        if (e == null) return

        when (e.component.name) {
            "AddShotButton" -> onAddShotButtonClick()
            "ClearShotsButton" -> onClearShotsButtonClick()
            "ShowShotsButton" -> onShowShotsButtonClick()
            "CreateVideoButton" -> onCreateVideoButtonClick()

            else -> {
                throw IllegalArgumentException("Cannot recognize button name")
            }
        }
        refreshShotsCount()
    }

    private fun onShowShotsButtonClick() {
        _shotsListWindow.isVisible = true
    }

    private fun onClearShotsButtonClick() {
        VideoSettings.dispose();
        _shotsListWindow.apply {
            dispose()
            repaint()
            isVisible = true
        }
    }

    private fun onAddShotButtonClick() {
        val plane = _mainWindow.plane
        val newShot = Shot(Plane(plane.xMin, plane.xMax, plane.yMin, plane.yMax))

        VideoSettings.addShot(newShot)
        _shotsListWindow.apply {
            updateThumbnails(VideoSettings.getKeyShots())
            repaint()
            isVisible = true
        }
    }

    private fun refreshShotsCount(count: Int = -1) {
        if (count != -1) _mainWindow.shotsCountLabel.text = "Shots count: ${count}";
        else _mainWindow.shotsCountLabel.text = "Shots count: ${VideoSettings.getKeyShotsCount()}";
    }

    private fun onCreateVideoButtonClick() {

        // todo: onClose (bug: starting video creation)
        // todo: alert if shotsCount == 0

        val videoHeight = _mainWindow.heightSpinner.value as Int
        val videoWidth = _mainWindow.widthSpinner.value as Int
        val videoFramerate = _mainWindow.fpsSpinner.value as Int
        val videoDuration = _mainWindow.durationSpinner.value as Int

        val fileChooser = JFileChooser().apply { selectedFile = File("video.mp4") };

        fileChooser.showSaveDialog(_mainWindow)

        var filename = fileChooser.selectedFile.absolutePath;

        if (!filename.contains(".mp4"))
            filename = filename.plus(".mp4");


        val createModel = CreateVideoModel(
            videoHeight,
            videoWidth,
            videoDuration,
            videoFramerate,
            filename
        )

        VideoSettings.requiredShotsCount = videoFramerate * videoDuration - 1;

        // todo: вызывать в отдельном потоке
       // _progressWindow.isVisible = true;

        _service.execute(createModel)
    }
}