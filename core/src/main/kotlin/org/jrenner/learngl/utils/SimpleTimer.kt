package org.jrenner.learngl.utils

import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.GdxRuntimeException

class SimpleTimer(val name: String) {
    var total = 0L
    var count = 0

    var active = false

    private var startTime = 0L

    fun start() {
        if (active) throw GdxRuntimeException("already started!")
        active = true
        startTime = TimeUtils.nanoTime()
    }

    fun stop() {
        if (!active) throw GdxRuntimeException("not started!")
        active = false
        val elapsed = TimeUtils.nanoTime() - startTime
        total += elapsed
        count++
    }

    private fun Long.fmt(): String {
        return "%.2f ms".format(this.toDouble() / 1000000.0)
    }


    fun report() {
        val avg: Long
        if (count == 0) {
            avg = -1L
        } else {
            avg = total / count
        }
        println("Timer [$name]: count: $count, total: ${total.fmt()}, average: ${avg.fmt()}")
    }

    fun reset() {
        total = 0L
        count = 0
        active = false
    }

}
