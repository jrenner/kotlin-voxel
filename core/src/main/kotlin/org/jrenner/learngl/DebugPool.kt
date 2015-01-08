package org.jrenner.learngl

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.Array as Arr

class DebugPool<T>(val newObjFunc: () -> T, initialCapacity: Int = 16) : Pool<T>(initialCapacity) {
    var objectsCreated = 0
        private set

    var objectsFreed = 0

    var objectsObtained = 0

    override fun obtain(): T {
        objectsObtained++
        return super.obtain()
    }

    override fun newObject(): T {
        objectsCreated++
        return newObjFunc()
    }

    override fun free(obj: T) {
        super.free(obj)
        objectsFreed++
    }

    override fun freeAll(objects: Arr<T>?) {
        throw GdxRuntimeException("DebugPool does not support freeAll method")
    }

    fun debugInfo(): String {
        return "created: $objectsCreated, obtained: $objectsObtained, freed: $objectsFreed, currently free: ${getFree()}"
    }
}

