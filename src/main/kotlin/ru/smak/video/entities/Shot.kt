package ru.smak.video.entities

import kotlinx.coroutines.*
import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import ru.smak.gui.MainWindow
import ru.smak.math.FractalFuncs
import ru.smak.math.Mandelbrot
import ru.smak.video.objects.VideoSettings
import java.awt.image.BufferedImage

class Shot(plane: Plane) {

    val plane: Plane;
    var image: Deferred<BufferedImage>? = null;
    var thumbnailImage: BufferedImage;
    private val _scope = CoroutineScope(Dispatchers.Default)

    init {
        this.plane = plane;

        thumbnailImage = getImageFromPlane(this.plane,100, 100);
        image = getImageFromPlaneAsync(_scope, VideoSettings.width, VideoSettings.height);
    }

    companion object {
        fun getImageFromPlane(plane: Plane, width: Int, height: Int): BufferedImage {
            plane.width = width;
            plane.height = height;

            val fp = FractalPainter(FractalFuncs[0], MainWindow.colorScheme, plane)

            val img = BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
            );

            fp.paint(img.graphics)

            return img
        }
    }

    private fun getImageFromPlaneAsync(scope: CoroutineScope, width: Int, height: Int) = scope.async {
        return@async getImageFromPlane(plane, width, height);
    }

}