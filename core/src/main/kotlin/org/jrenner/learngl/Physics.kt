package org.jrenner.learngl

import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.gameworld.CubeData
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.collision.BoundingBox

/** returns position correction needed */
object Physics {

    private val tmp = Vector3()
    private val tmp2 = Vector3()
    private val tmp3 = Vector3()
    private val tmp4 = Vector3()

    private val intersect = Vector3()

    private val ray = Ray(tmp, tmp)
    private val bbox = BoundingBox()

    val rayStart = Vector3()
    val rayDir = Vector3()
    val rayEnd = Vector3()

    fun collision(pos: Vector3): Vector3 {
        tmp.setZero()
        if (world.hasChunkAt(pos.x, pos.y, pos.z)) {
            val center = tmp2.set(world.getCubeAt(pos.x, pos.y, pos.z).getPositionTempVec()).add(0.5f, 0.5f, 0.5f)
            val diff = tmp.set(center).sub(pos)
            rayStart.set(diff.scl(2f)).add(pos)
            rayDir.set(diff)
            rayEnd.set(rayDir).scl(10f).add(rayStart)
            ray.set(rayStart, rayDir)
            bbox.set(tmp3.set(center.x - 0.5f, center.y - 0.5f, center.z - 0.5f),
                    tmp4.set(center.x + 0.5f, center.y + 0.5f, center.z + 0.5f))

            val didHit = Intersector.intersectRayBounds(ray, bbox, intersect)

            if (didHit) {
                //println("hit: $didHit \t intersection: $intersect")
            }
        }
        return intersect
    }

    fun update() {
        collision(view.camera.position)
    }
}