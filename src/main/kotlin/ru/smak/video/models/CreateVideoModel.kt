package ru.smak.video.models

/*
* Модель окна создания видео.
* */
data class CreateVideoModel
(
    // Высота выходного видео в пикселях.
    val height: Int,

    // Ширина выходного видео в пикселях.
    val width: Int,

    // Длина видео в секундах.
    val duration: Int,

    // Количество FPS.
    val fps: Int = 24,

    // Имя сохраняемого файла.
    val filename: String
)
