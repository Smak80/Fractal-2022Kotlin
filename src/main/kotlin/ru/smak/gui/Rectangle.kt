package ru.smak.gui

import java.awt.Point
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

class Rectangle {

    private var p1: Point? = null
    private var p2: Point? = null

    val isExistst: Boolean
        get() = p1?.let { p2?.let { true } } ?: false

    val x1: Int?
        get() = p1?.let { min(it.x, p2?.x ?: Int.MAX_VALUE) }
    val x2: Int?
        get() = p1?.let { max(it.x, p2?.x ?: Int.MIN_VALUE) }
    val y1: Int?
        get() = p1?.let { min(it.y, p2?.y ?: Int.MAX_VALUE) }
    val y2: Int?
        get() = p1?.let { max(it.y, p2?.y ?: Int.MIN_VALUE) }

    val leftTop: Point?
        get() = x1?.let { x -> y1?.let{ y -> Point(x, y) } }
    val rightBottom: Point?
        get() = x2?.let { x -> y2?.let{ y -> Point(x, y) } }

    val width: Int
        get() = p1?.let { pt1 -> p2?.let { pt2 -> abs(pt1.x - pt2.x) } } ?: 0
    val height: Int
        get() = p1?.let { pt1 -> p2?.let { pt2 -> abs(pt1.y - pt2.y) } } ?: 0

    fun addPoint(p: Point){
        p1?.let { if (p1 != p) p2 = p else p2 = null } ?: run{ p1 = p }
    }

    fun destroy(){
        p1 = null
        p2 = null
    }
}