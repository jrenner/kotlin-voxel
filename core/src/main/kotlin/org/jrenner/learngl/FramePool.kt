package org.jrenner.learngl

import com.badlogic.gdx.utils.Array as Arr

/** a pool of re-usable objects, where the re-usable objects are only borrowed until
 * the start of the next render frame, when they are automatically reset and added back
 * into the pool
 */
abstract class FramePool<T>() {
    class object {
        val framePools = Arr<FramePool<Any?>>()
        fun reset() {
            for (fp in framePools) {
                fp.reset()
            }
        }
    }

    private val pool = Arr<T>()
    private val borrowed = Arr<T>()

    abstract fun newObject(): T

    fun obtain(): T {
        val item: T = if (pool.size == 0) {
            newObject()
        } else {
            pool.pop()
        }
        borrowed.add(item)
        return item
    }

    fun reset() {
        for (i in 0..borrowed.size-1) {
            val item = borrowed[i]
            pool.add(item)
        }
        borrowed.clear()
    }

}