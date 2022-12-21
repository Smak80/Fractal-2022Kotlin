package ru.smak.math


val mandelbrot = Mandelbrot()
val julia = Julia()
val nova = RandomFractal()

val FractalFuncs = mutableListOf<(Complex)->Float>(
    mandelbrot::IsInSet,
    julia::IsInSet,
    nova::IsInSet
)