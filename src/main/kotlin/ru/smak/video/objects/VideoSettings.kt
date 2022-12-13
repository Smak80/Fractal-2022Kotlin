package ru.smak.video.objects

import kotlinx.coroutines.DisposableHandle
import ru.smak.video.entities.Shot

// класс параметров для создания видео
object VideoSettings : DisposableHandle {

    private val _shotsList = mutableListOf<Shot>(); // список ключевых снимков (добавляются при нажатии на Add Shot кнопку)

    var width = 800;
    var height = 600;
    var requiredShotsCount = 0;

    fun getKeyShotsCount() = _shotsList.size;
    fun getKeyShots() = _shotsList.toMutableList();

    fun addShot(shot: Shot) = _shotsList.add(shot);
    fun deleteShot(shot: Shot) = _shotsList.remove(shot);

    override fun dispose() {
        _shotsList.clear();
    }
}