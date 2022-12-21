package ru.smak.gui

import ru.smak.graphics.Plane
import java.awt.Dimension

// На самом деле класс содержит не размер, а точные координаты панели :)
class TargetSz() {
    var targetXMin = 0.0
    var targetXMax = 0.0
    var targetYMin = 0.0
    var targetYMax = 0.0
    //Задаёт целевой размер от панели
    fun getTargetFromPlane(plane: Plane)
    {
        targetXMin=plane.xMin
        targetXMax=plane.xMax
        targetYMin=plane.yMin
        targetYMax=plane.yMax
    }
    //Задаёт панели целевой размер
    fun makePlaneFromTarger(plane: Plane)
    {
        plane.yEdges=Pair(targetYMin,targetYMax)
        plane.xEdges=Pair(targetXMin,targetXMax)
    }
    //Двигает панель и таргет сз
    fun shiftImage(shiftX: Double, shiftY: Double, plane: Plane) {
        targetXMin -= shiftX
        targetXMax -= shiftX
        targetYMin -= shiftY
        targetYMax -= shiftY
        makePlaneFromTarger(plane)
    }
}
//Меняет целевой размер
fun makeOneToOne(plane : Plane,x0: Double,x1:Double,y0:Double,y1:Double,dimension: Dimension,newTargetSz: TargetSz)
{
    newTargetSz.targetXMin=x0
    newTargetSz.targetXMax=x1
    newTargetSz.targetYMin=y0
    newTargetSz.targetYMax=y1
    makeOneToOne(plane, newTargetSz,dimension)
}
//Не меняет!
fun makeOneToOne(plane : Plane, trgSz: TargetSz,dimension: Dimension)
{
    trgSz.makePlaneFromTarger(plane)
    makeOneToOne(plane,trgSz.targetXMin,trgSz.targetXMax,trgSz.targetYMin,trgSz.targetYMax,dimension)
}
//Не меняет!
fun makeOneToOne(plane : Plane, x0: Double,x1:Double,y0:Double,y1:Double,dimension: Dimension,)//:Pair<Pair<Double,Double>,Pair<Double,Double>>
{
    val x2w = Math.abs(x1-x0)/dimension.width
    val y2h = Math.abs(y1-y0)/dimension.height
    if(y2h>x2w)
    {
        val dx = (dimension.width*y2h-Math.abs(x1-x0))/2
        plane.xEdges=Pair(x0-dx,x1+dx)
        plane.yEdges=Pair(y0,y1)
    }
    else
    {
        val dy = (dimension.height*x2w-Math.abs((y1-y0)))/2
        plane.yEdges=Pair(Math.max(y0,y1)+dy,Math.min(y1,y0)-dy)
        plane.xEdges=Pair(x0,x1)
    }
}
