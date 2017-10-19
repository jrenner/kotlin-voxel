package org.jrenner.learngl.gameworld

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array as Arr
import org.jrenner.learngl.cube.CubeDataGrid
import org.jrenner.learngl.View
import org.jrenner.learngl.utils.threeIntegerHashCode
import com.badlogic.gdx.math.Frustum
import com.badlogic.gdx.utils.IntSet
import org.jrenner.learngl.utils.inFrustum
import org.jrenner.learngl.world

/** updates chunks in the world on a separate Thread */
class WorldUpdater(val wor: World): Runnable {

    /** hold here to pass to world thread when finished updating */
    val tempCreationQueue = Arr<CubeDataGrid>()
    /** this temporarily stores data from the World's map of chunk hash codes -> chunks
     * for thread safety.
     * It is used to check if the chunks is either already created
     * or already queued for creation
     * the world updater
     */
    val tempChunkHashCodeSet = IntSet()
    val tempChunks = Arr<Chunk>()

    /** camPos and frustum will be set by the View in View.render */
    val tempCamPos = Vector3()
    val tempFrustum = Frustum()

    val updateIntervalMillis = 250L
    var maxDist = View.maxViewDist
    val queueLimit = 10
    var worldQueueSize = 0

    override fun run() {
        try {
            while (true) {
                synchronized(wor) {
                    retrieveDataFromWorld()
                }
                    createChunksInViewRange()
                synchronized(wor) {
                    communicateUpdateToWorld()
                }
                Thread.sleep(updateIntervalMillis)
            }
        } catch (e: Exception) {
            // if we have failed at thread-safety, just crash with a message
            println("ERROR in WorldUpdater thread:\n")
            e.printStackTrace()
            System.exit(1)
        }
    }

    fun retrieveDataFromWorld() {
        tempChunkHashCodeSet.clear()
        for (item in wor.chunkHashCodeMap.keys()) {
            tempChunkHashCodeSet.add(item)
        }
        for (item in wor.chunkCreationQueue) {
            val hash = item.hashCode()
            tempChunkHashCodeSet.add(hash)
        }
        worldQueueSize = wor.chunkCreationQueue.size
    }


    fun communicateUpdateToWorld() {
        wor.processUpdateFromWorldUpdater(this)
        tempCreationQueue.clear()
    }

    /** see: World.hasChunkAt */
    fun worldUpdaterHasChunkAt(x: Float, y: Float, z: Float): Boolean {
        val sx = wor.snapToChunkOrigin(x).toInt()
        val sy = wor.snapToChunkOrigin(y).toInt()
        val sz = wor.snapToChunkOrigin(z).toInt()
        return tempChunkHashCodeSet.contains(threeIntegerHashCode(sx, sy, sz))
    }

    fun createChunksInViewRange() {
        if (worldQueueSize > queueLimit) return
        /*if (chunkCreationQueue.size >= queueSizeLimit) {
            return
        }*/
        val camPos = tempCamPos
        val loX = MathUtils.clamp(camPos.x - maxDist, 0f, world.width.toFloat());
        val loY = MathUtils.clamp(camPos.y - maxDist, 0f, world.height.toFloat());
        val loZ = MathUtils.clamp(camPos.z - maxDist, 0f, world.depth.toFloat());
        val hiX = MathUtils.clamp(camPos.x + maxDist, 0f, world.width.toFloat());
        val hiY = MathUtils.clamp(camPos.y + maxDist, 0f, world.height.toFloat());
        val hiZ = MathUtils.clamp(camPos.z + maxDist, 0f, world.depth.toFloat());
        val sz: Float = Chunk.chunkSizef

        fun createChunkIfNeeded(x: Float, y: Float, z: Float) {
            val chunkX = wor.snapToChunkCenter(x)
            val chunkY = wor.snapToChunkCenter(y)
            val chunkZ = wor.snapToChunkCenter(z)
            // does this chunk already exist?
            val hasChunk = worldUpdaterHasChunkAt(chunkX, chunkY, chunkZ)
            // lazily create chunks only when the camera looks at them
            val inView = inFrustum(chunkX, chunkY, chunkZ, Chunk.chunkSizef, tempFrustum)
            if (!hasChunk && inView) {
                val dist2 = camPos.dst2(chunkX, chunkY, chunkZ)
                // IN RANGE
                if (dist2 <= maxDist * maxDist) {
                    val origin = Vector3(wor.snapToChunkOrigin(x), wor.snapToChunkOrigin(y), wor.snapToChunkOrigin(z))
                    val cdg = wor.worldData.getCDGByWorldPos(origin.x, origin.y, origin.z)
                    //println("WORLD UPDATER: added to temp queue (${origin.x}, ${origin.y}, ${origin.z})")
                    tempCreationQueue.add(cdg)
                }
            }
        }
        var x = loX
        var y: Float
        var z: Float
        while(x <= hiX) {
            y = loY
            while(y <= hiY) {
                z = loZ
                while (z <= hiZ) {
                    createChunkIfNeeded(x, y, z)
                    z +=sz
                }
                y += sz
            }
            x += sz
        }
        /*
            Original code used for iteration:
            for (x in loX..hiX step sz) {
                for (y in loY..hiY step sz) {
                    for (z in loZ..hiZ step sz) {
                        ...
            Kotlin converts the float primitives into boxed Float objects in order to use Float method invocations
            Current iteration method avoids object allocation (GC optimization)
         */
    }
}
