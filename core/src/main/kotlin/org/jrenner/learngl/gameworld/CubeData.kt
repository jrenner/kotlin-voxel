package org.jrenner.learngl.gameworld

import com.badlogic.gdx.utils.Array as Arr
import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.Direction
import org.jrenner.learngl

enum class CubeType {
    Grass
    Water
    Void // inactive or void cube
}


class CubeData {
    class object {
        private val tmp = Vector3()
    }
    public var x: Float = 0f
    public var y: Float = 0f
    public var z: Float = 0f
    var cubeType = CubeType.Void
    var hiddenFaces: Int = 0
    val hiddenFacesCount: Int
        get() {
            var c = 0
            for (i in 0..Direction.allSize -1) {
                if (hiddenFaces and Direction.all[i] != 0) {
                    c++
                }
            }
            return c
        }

    val debugHiddenFaces: String
        get() {
            var hidden = ""
            for (i in 0..Direction.allSize -1) {
                val dir = Direction.all[i]
                if (hiddenFaces and dir != 0) {
                    hidden += ", ${Direction.toString(dir)}"
                }
            }
            return hidden
        }

    fun getPositionTempVec(): Vector3 {
        return tmp.set(x, y, z)
    }
}

