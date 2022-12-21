package ru.smak.video.models

import ru.smak.video.entities.Shot

/*
* Модель окна создания видео.
* */
data class CreateVideoModel(
    // Высота выходного видео в пикселях.
    val height: Int,

    // Ширина выходного видео в пикселях.
    val width: Int,

    // Длина видео в секундах.
    val duration: Int,

    // Количество FPS.
    val fps: Int = 24,

    // Имя сохраняемого файла.
    val filename: String,

    // Ключевые кадры
    val keyShots: MutableList<Shot>
)
