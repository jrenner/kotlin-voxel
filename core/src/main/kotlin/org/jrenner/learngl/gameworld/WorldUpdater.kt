package org.jrenner.learngl.gameworld

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array as Arr
import org.jrenner.learngl.cube.CubeDataGrid
import org.jrenner.learngl
import com.badlogic.gdx.utils
import org.jrenner.learngl.View

/** updates chunks in the world on a separate Thread */
class WorldUpdater(val wor: World): Runnable {

    /** hold here to pass to world thread when finished updating */
    val tempCreationQueue = utils.Array<CubeDataGrid>()

    override fun run() {
        try {
            while (true) {
                reset()
                createChunksInViewRange()
                communicateUpdateToWorld()
                Thread.sleep(500)
            }
        } catch (e: Exception) {
            println("Error in world updter thread:\n")
            e.printStackTrace()
            System.exit(1)
        }
    }

    fun communicateUpdateToWorld() {
        synchronized(wor) {
            wor.processUpdateFromWorldUpdater(this)
        }
    }

    fun reset() {
        tempCreationQueue.clear()
    }


    fun createChunksInViewRange() {
        /*if (chunkCreationQueue.size >= queueSizeLimit) {
            return
        }*/
        val maxDist = View.maxViewDist
        val camPos = learngl.view.camera.position
        val loX = MathUtils.clamp(camPos.x - maxDist, 0f, learngl.world.width.toFloat());
        val loY = MathUtils.clamp(camPos.y - maxDist, 0f, learngl.world.height.toFloat());
        val loZ = MathUtils.clamp(camPos.z - maxDist, 0f, learngl.world.depth.toFloat());
        val hiX = MathUtils.clamp(camPos.x + maxDist, 0f, learngl.world.width.toFloat());
        val hiY = MathUtils.clamp(camPos.y + maxDist, 0f, learngl.world.height.toFloat());
        val hiZ = MathUtils.clamp(camPos.z + maxDist, 0f, learngl.world.depth.toFloat());
        val sz: Float = Chunk.chunkSizef

        fun createChunkIfNeeded(x: Float, y: Float, z: Float) {
            val chunkX = wor.snapToChunkCenter(x)
            val chunkY = wor.snapToChunkCenter(y)
            val chunkZ = wor.snapToChunkCenter(z)
            // does this chunk already exist?
            val hasChunk = wor.hasChunkAt(chunkX, chunkY, chunkZ)
            // lazily create chunks only when the camera looks at them
            if (!hasChunk && learngl.view.inFrustrum(chunkX, chunkY, chunkZ, Chunk.chunkSizef)) {
                val dist2 = camPos.dst2(chunkX, chunkY, chunkZ)
                // IN RANGE
                if (dist2 <= maxDist * maxDist) {
                    if (hasChunk) {
                        // do nothing
                    } else {
                        val origin = Vector3(wor.snapToChunkOrigin(x), wor.snapToChunkOrigin(y), wor.snapToChunkOrigin(z))
                        val cdg = wor.worldData.getCDGByWorldPos(origin.x, origin.y, origin.z)
                        //println("WORLD UPDATER: added to temp queue (${origin.x}, ${origin.y}, ${origin.z})")
                        tempCreationQueue.add(cdg)
                    }
                }
            }
        }

        /*
            Original code used for iteration:
            for (x in loX..hiX step sz) {
                for (y in loY..hiY step sz) {
                    for (z in loZ..hiZ step sz) {
                        ...

            Kotlin converts the floats into Float objects in order to use Float method invocations
            Current iteration method avoids object allocation (GC optimization)

         */
        var x = loX
        var y: Float
        var z: Float
        while(x <= hiX) {
            y = loY
            while(y <= hiY) {
                z = loZ
                while (z <= hiZ) {
                    // at each iteration we abandon if the queue is already over the limit
                    /*                if (chunkCreationQueue.size >= queueSizeLimit) {
                                        return
                                    }*/
                    createChunkIfNeeded(x, y, z)
                    z +=sz
                }
                y += sz
            }
            x += sz
        }
    }
}
