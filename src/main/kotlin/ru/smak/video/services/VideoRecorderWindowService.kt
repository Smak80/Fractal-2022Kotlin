package ru.smak.video.services

import org.jcodec.api.SequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.ColorSpace
import org.jcodec.common.model.Rational
import org.jcodec.scale.AWTUtil
import ru.smak.graphics.Plane
import ru.smak.gui.MainWindow
import ru.smak.math.Complex
import ru.smak.math.Mandelbrot
import ru.smak.math.video.catmullRom
import ru.smak.math.video.lerpUnclamped
import ru.smak.math.video.smoothOutExp
import ru.smak.video.entities.Shot
import ru.smak.video.objects.VideoSettings
import ru.smak.video.models.CreateVideoModel
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.stream.Collectors
import javax.swing.JProgressBar
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class VideoRecorderWindowService {

    private var _frameHeight: Int = 0
    private var _frameWidth: Int = 0
    private var _planeHeight: Int = 0
    private var _planeWidth: Int = 0
    private val _threadCount = 16
    private val _taskCount = _threadCount * 4
    private val _threadPool = Executors.newFixedThreadPool(_threadCount)
    private val _fractal = Mandelbrot()
    private val _colorizer = MainWindow.colorScheme;

    val aspectRatio
        get() = _frameWidth.toDouble() / _frameHeight

    @OptIn(ExperimentalTime::class)
    fun execute(model: CreateVideoModel) {
        _frameWidth = model.width
        _frameHeight = model.height

        println("Video create started")
        val executedTime = measureTime {
            val bufferedImages = getData(model)

            create(bufferedImages, model)
        }

        println("Video created!")
        println("Time: ${executedTime.inWholeMilliseconds / 1000f} sec")
    }

    @Suppress("Since15")
    private fun getData(model: CreateVideoModel): List<BufferedImage> {
        val keyShots = VideoSettings.getKeyShots()

        if (VideoSettings.getKeyShotsCount() == 0) {
            throw Exception("Empty key shots list")
        }

        val centersOfShots = getCenterOfShots(keyShots)
        val firstShot = keyShots[0]
        val lastShot = keyShots[VideoSettings.getKeyShotsCount() - 1]
        _planeWidth = firstShot.plane.width
        _planeHeight = firstShot.plane.height

        val firstShotHeight = firstShot.run { max(plane.xMax - plane.xMin, plane.yMax - plane.yMin) }
        val lastShotHeight = lastShot.run { max(plane.xMax - plane.xMin, plane.yMax - plane.yMin) }
        val linearZoom = ln(lastShotHeight) - ln(firstShotHeight)
        val shotsCount = model.fps * model.duration - 1

        return (0..shotsCount).toList().parallelStream()
            .map { getInBetween(it, shotsCount, centersOfShots, firstShotHeight, linearZoom) }
            .map { plane ->
                val image = Shot.getImageFromPlane(plane, _frameWidth, _frameHeight)
                image
            }
            .collect(Collectors.toList())
    }

    private fun create(data: List<BufferedImage>, model: CreateVideoModel) {
        val output = File(model.filename);

        val enc = SequenceEncoder.createWithFps(
            NIOUtils.writableChannel(output),
            Rational(model.fps, 1)
        );

        // TODO: Распараллелить
        // todo: если только один шот (исходное положение фрактала, то вылазит эксепшн)
        for (img in data)
            enc.encodeNativeFrame(AWTUtil.fromBufferedImage(img, ColorSpace.RGB));

        enc.finish()
    }

    private fun getCenterOfShots(shots: MutableList<Shot>): List<Complex> {
        return VideoSettings.getKeyShots().map {
            Complex(
                (it.plane.xMin + it.plane.xMax) * 0.5,
                (it.plane.yMin + it.plane.yMax) * 0.5
            )
        }
    }

    private fun getInBetween(
        frameNumber: Int, lastFrameNumber: Int, centers: List<Complex>, firstShotHeight: Double, linearZoom: Double
    ): Plane {
        val t = frameNumber.toDouble() / lastFrameNumber
        val center = catmullRom(smoothOutExp(0.0, 1.0, t), centers)
        val zoom = exp(lerpUnclamped(0.0, linearZoom, t))
        val deltaHeight = firstShotHeight * zoom * 0.5
        val deltaWidth = deltaHeight * aspectRatio

        val result = Plane(
            center.re - deltaWidth, center.re + deltaWidth,
            center.im - deltaHeight, center.im + deltaHeight
        )
            .apply {
                width = _frameWidth
                height = _frameHeight
            }

        return result
    }

//    private fun drawImageAt(plane: Plane): BufferedImage {
////        val image = BufferedImage(_frameWidth, _frameHeight, BufferedImage.TYPE_INT_RGB)
////        paint(image.graphics, plane)
////        return image
//
//        return
//    }

    // todo: useless?
//    private fun paint(g: Graphics, plane: Plane) {
//        if (plane.width == 0 || plane.height == 0) return
//        val stripWidth = plane.width / _taskCount
//        List(_taskCount) {
//            _threadPool?.submit(Callable {
//                val start = it * stripWidth
//                val end = (it + 1) * stripWidth - 1 + if (it + 1 == _taskCount) plane.width % _taskCount else 0
//                val image = BufferedImage(end - start + 1, plane.height, BufferedImage.TYPE_INT_RGB)
//                val ig = image.graphics
//                for (i in start..end) {
//                    for (j in 0..plane.height) {
//                        val fc = _fractal.isInSet(
//                            Complex(
//                                Converter.xScrToCrt(i, plane),
//                                Converter.yScrToCrt(j, plane)
//                            )
//                        )
//                        ig.color = _colorizer(fc)
//                        ig.fillRect(i - start, j, 1, 1)
//                    }
//                }
//                image
//            })
//        }.forEachIndexed { i, v -> g.drawImage(v?.get(), i * stripWidth, 0, null) }
//    }

    private data class ImageData(val frame: Int, val lastFrame: Int)
}