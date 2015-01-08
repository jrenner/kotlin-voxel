package org.jrenner.learngl.utils

import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.cube.CubeDataGrid
import org.jrenner.learngl.Direction
import com.badlogic.gdx.utils.StringBuilder
import org.jrenner.learngl
import org.jrenner.learngl.gameworld.World
import org.jrenner.learngl.gameworld.CubeData
import org.jrenner.learngl.gameworld.CubeType

class IntVector3 {
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0

    fun set(x: Int, y: Int, z: Int): IntVector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(v: IntVector3): IntVector3 {
        return set(v.x, v.y, v.z)
    }

    fun set(v: Vector3): IntVector3 {
        return set(v.x.toInt(), v.y.toInt(), v.z.toInt())
    }

    fun toVector3(v: Vector3) {
        v.set(x.toFloat(), y.toFloat(), z.toFloat())
    }
}

fun World.get(v3: Vector3): CubeData {
    return this.getCubeAt(v3.x, v3.y, v3.z)
}

fun CubeDataGrid.refresh() {
    if (dirty) {
        dirty = false
        calculateHiddenFaces(learngl.world)
    }
}

fun CubeDataGrid.calculateHiddenFaces(wor: World = learngl.world) {
    for (cubeData: CubeData in this) {
        cubeData.hiddenFaces = 0
        if (!learngl.hiddenFacesEnabled) continue
        if (cubeData.cubeType == CubeType.Void) {
            continue
            //cubeData.hiddenFaces = Direction.ALL_FACES
        } else {
            for (i in 0..Direction.allSize - 1) {
                var x = cubeData.x
                var y = cubeData.y
                var z = cubeData.z
                val dir = Direction.all[i]
                when (dir) {
                    Direction.Up -> y++
                    Direction.Down -> y--
                    Direction.North -> z++
                    Direction.South -> z--
                    Direction.West -> x--
                    Direction.East -> x++
                }
                // only care about hidden faces local to this cube
                if (this.hasCubeAt(x, y, z)) {
                    val cube = getCubeAt(x, y, z)
                    if (cube.cubeType != CubeType.Void) {
                        // HIDDEN
                        cubeData.hiddenFaces = cubeData.hiddenFaces or dir
                    }
                }
            }
        }
    }
}

fun StringBuilder.plus(any: Any?): StringBuilder {
    return this.append(any)
}

val Float.fmt: String get() = "%.2f".format(this)
val Vector3.fmt: String get() = "%.2f, %.2f, %.2f".format(x, y, z)