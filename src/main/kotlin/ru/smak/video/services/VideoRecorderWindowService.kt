package ru.smak.video.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jcodec.api.SequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.ColorSpace
import org.jcodec.common.model.Rational
import org.jcodec.scale.AWTUtil
import ru.smak.graphics.Plane
import ru.smak.math.Complex
import ru.smak.math.video.catmullRom
import ru.smak.math.video.lerpUnclamped
import ru.smak.math.video.smoothOutExp
import ru.smak.video.entities.Shot
import ru.smak.video.events.VideoRecorderEvent
import ru.smak.video.models.CreateVideoModel
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import java.util.stream.Collectors
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

internal val videoRecordingStarted = VideoRecorderEvent<Unit>()
internal val imageCreated = VideoRecorderEvent<Int>()
internal val imageCreatingFinished = VideoRecorderEvent<Unit>()
internal val framesCreated = VideoRecorderEvent<Unit>()
internal val videoRecordingProcessing = VideoRecorderEvent<Unit>()
internal val videoRecordingFinished = VideoRecorderEvent<String>()

class VideoRecorderWindowService
{
    private var _frameHeight: Int = 0
    private var _frameWidth: Int = 0
    private var _planeHeight: Int = 0
    private var _planeWidth: Int = 0

    val aspectRatio
        get() = _frameWidth.toDouble() / _frameHeight

    @OptIn(ExperimentalTime::class)
    fun execute(model: CreateVideoModel) {
        _frameWidth = model.width
        _frameHeight = model.height

        videoRecordingStarted(Unit)
        val executedTime = measureTime {
            val bufferedImages = getData(model)
            imageCreatingFinished(Unit)

            runBlocking {
                delay(100)
            }

            create(bufferedImages, model)
        }

        videoRecordingFinished("Time: ${executedTime.inWholeMilliseconds / 1000f} sec")
    }

    @Suppress("Since15")
    private fun getData(model: CreateVideoModel): List<BufferedImage> {
        val keyShots = model.keyShots

        if (keyShots.size == 0) {
            throw Exception("Empty key shots list")
        }

        val centersOfShots = getCenterOfShots(keyShots)
        val firstShot = keyShots[0]
        val lastShot = keyShots[keyShots.size - 1]
        _planeWidth = firstShot.plane.width
        _planeHeight = firstShot.plane.height

        val firstShotHeight = firstShot.run { max(plane.xMax - plane.xMin, plane.yMax - plane.yMin) }
        val lastShotHeight = lastShot.run { max(plane.xMax - plane.xMin, plane.yMax - plane.yMin) }
        val linearZoom = ln(lastShotHeight) - ln(firstShotHeight)
        val shotsCount = model.fps * model.duration - 1

        return (0..shotsCount).toList().parallelStream()
            .map { getInBetween(it, shotsCount, centersOfShots, firstShotHeight, linearZoom) }
            .map { (frameNumber, plane) ->
                val image = Shot.getImageFromPlane(plane, _frameWidth, _frameHeight)
                imageCreated(frameNumber)
                image
            }
            .collect(Collectors.toList())
    }

    private fun create(data: List<BufferedImage>, model: CreateVideoModel) {
        val output = File(model.filename);

        val enc = SequenceEncoder.createWithFps(
            NIOUtils.writableChannel(output),
            Rational(model.fps, 1)
        )
        framesCreated(Unit)

        for (img in data) {
            enc.encodeNativeFrame(AWTUtil.fromBufferedImage(img, ColorSpace.RGB));
            videoRecordingProcessing(Unit)
        }

        enc.finish()
    }

    private fun getCenterOfShots(shots: MutableList<Shot>): List<Complex> {
        return shots.map {
            Complex(
                (it.plane.xMin + it.plane.xMax) * 0.5,
                (it.plane.yMin + it.plane.yMax) * 0.5
            )
        }
    }

    private fun getInBetween(
        frameNumber: Int, lastFrameNumber: Int, centers: List<Complex>, firstShotHeight: Double, linearZoom: Double
    ): ImageData {
        val t = frameNumber.toDouble() / lastFrameNumber
        val center = catmullRom(smoothOutExp(0.0, 1.0, t), centers)
        val zoom = exp(lerpUnclamped(0.0, linearZoom, t))
        val deltaHeight = firstShotHeight * zoom * 0.5
        val deltaWidth = deltaHeight * aspectRatio

        val resultPlane = Plane(
            center.re - deltaWidth, center.re + deltaWidth,
            center.im - deltaHeight, center.im + deltaHeight
        )
            .apply {
                width = _frameWidth
                height = _frameHeight
            }

        return ImageData(frameNumber, resultPlane)
    }
}

internal data class ImageData(val frameNumber: Int, val plane: Plane)
