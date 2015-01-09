package org.jrenner.learngl.utils

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Frustum

fun r(f: Float) = MathUtils.random(f)
fun r(f1: Float, f2: Float) = MathUtils.random(f1, f2)
fun r(n: Int) = MathUtils.random(n)
fun r(n1: Int, n2: Int) = MathUtils.random(n1, n2)

fun randomizeColor(color: Color, min: Float = 0.0f, max: Float = 1.0f): Color {
    color.r = r(min, max)
    color.g = r(min, max)
    color.b = r(min, max)
    return color
}

public fun threeIntegerHashCode(a: Int, b: Int, c: Int): Int {
    // h = (a*P1 + b)*P2 + c
    val prime1 = 1013
    val prime2 = 7499
    return (a * prime1 + b) * prime2 + c
}

fun inFrustum(x: Float, y: Float, z: Float, bboxRadius: Float, frustum: Frustum): Boolean {
    val w = bboxRadius
    val h = bboxRadius
    val d = bboxRadius
    return frustum.boundsInFrustum(x, y, z, w, h, d)
}