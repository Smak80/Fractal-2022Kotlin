package ru.smak.math


val mandelbrot = Mandelbrot()
val julia = Julia()

val FractalFuncs = mutableListOf<(Complex)->Float>(
    mandelbrot::isInSet,
    julia::isInSet
)