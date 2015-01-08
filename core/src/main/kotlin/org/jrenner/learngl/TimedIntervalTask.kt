package org.jrenner.learngl

class TimedIntervalTask(val intervalSeconds: Float, val task: () -> Unit) {
    private var accumulated = 0f

    fun update(dt: Float) {
        accumulated += dt
        if (accumulated >= intervalSeconds) {
            accumulated -= intervalSeconds
            task()
        }
    }
}
