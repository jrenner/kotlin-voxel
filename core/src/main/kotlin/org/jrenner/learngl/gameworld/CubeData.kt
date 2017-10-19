package org.jrenner.learngl.gameworld

import com.badlogic.gdx.utils.Array as Arr
import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.Direction


class CubeData {
    companion object {
        private val tmp = Vector3()
    }
    public val xf: Float get() = x.toFloat()
    public val yf: Float get() = y.toFloat()
    public val zf: Float get() = z.toFloat()
    public var x: Short = 0
    public var y: Short = 0
    public var z: Short = 0
    var cubeType = CubeType.Void
    var hiddenFaces: Int = 0
    val hiddenFacesCount: Int
        get() {
            var c = 0
            for (i in 0 until Direction.allSize) {
                if (hiddenFaces and Direction.all[i] != 0) {
                    c++
                }
            }
            return c
        }

    val debugHiddenFaces: String
        get() {
            var hidden = ""
            for (i in 0 until Direction.allSize) {
                val dir = Direction.all[i]
                if (hiddenFaces and dir != 0) {
                    hidden += ", ${Direction.toString(dir)}"
                }
            }
            return hidden
        }

    fun getPositionTempVec(): Vector3 {
        return tmp.set(xf, yf, zf)
    }
}

