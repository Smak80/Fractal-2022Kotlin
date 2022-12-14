package ru.smak.video.events

class VideoRecorderEvent<T> {
    private val listeners = mutableListOf<(VideoRecorderEvent<T>, T) -> Unit>()

    fun addListener(listener: (VideoRecorderEvent<T>, T) -> Unit)
    {
        listeners.add(listener)
    }

    fun removeListener(listener: (VideoRecorderEvent<T>, T) -> Unit)
    {
        listeners.remove(listener)
    }

    fun clearListeners()
    {
        listeners.clear()
    }

    operator fun invoke(obj: T)
    {
        listeners.forEach { it(this, obj) }
    }
}