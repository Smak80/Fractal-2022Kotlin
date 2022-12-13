package ru.smak.video.operations

import ru.smak.video.objects.VideoSettings
import ru.smak.video.ui.windows.ShotsListWindow
import ru.smak.video.ui.windows.VideoWindow
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class ShotsThumbnailsWindowOperations(
    private val _mainComponent: VideoWindow,
    private val _window: ShotsListWindow,
) : MouseAdapter()
{
    override fun mouseClicked(e: MouseEvent?) {
        if (e == null)
        {
            return
        }

        when (e.component.name)
        {
            "DeleteShotButton" -> onDeleteShotButton()
            else -> {
                throw IllegalArgumentException("Cannot recognize button name")
            }
        }
        refreshShotsCount()
    }

    private fun onDeleteShotButton() {
        with(_window)
        {
            clearThumbs()
            for (selected in thumbnails.filter { it.isSelected }) {
                VideoSettings.deleteShot(selected.shot);
                thumbnails.remove(selected);
            }
            setupComponents();

            refreshShotsCount(thumbnails.size);
            thumbsPanel.repaint();
            isVisible = true;
        }
    }

    private fun clearThumbs() {
        with (_window)
        {
            for (thumb in thumbnails)
                thumbsPanel.remove(thumb);
        }
    }

    private fun setupComponents() {
        with (_window)
        {
            for (thumb in thumbnails)
                thumbsPanel.add(thumb);
        }
    }

    private fun refreshShotsCount(count: Int = -1) {
        if (count != -1) _mainComponent.shotsCountLabel.text = "Shots count: ${count}";
        else _mainComponent.shotsCountLabel.text = "Shots count: ${VideoSettings.getKeyShotsCount()}";
    }
}