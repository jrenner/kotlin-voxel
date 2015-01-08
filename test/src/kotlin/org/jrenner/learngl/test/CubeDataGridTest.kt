package org.jrenner.learngl.test

import org.junit.Test as test
import org.junit.Before as before
import org.junit.BeforeClass as beforeClass
import org.junit.Assert.*
import org.jrenner.learngl.cube.CubeDataGrid
import com.badlogic.gdx.utils.Array as Arr
import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.utils.IntVector3
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.ObjectSet
import org.jrenner.learngl.utils.threeIntegerHashCode
import org.jrenner.learngl


class CubeDataGridTest {

    fun basicCDG(): CubeDataGrid {
        val origin = Vector3(0f, 0f, 0f)
        return CubeDataGrid.create(origin.x, origin.y, origin.z)
    }

    val expectedNumElements = CubeDataGrid.width * CubeDataGrid.height * CubeDataGrid.depth


    test fun constructor() {
        val cdg = basicCDG()
        assertEquals(expectedNumElements, cdg.numElements)
    }

    test fun hashCodeTest() {
        val set = ObjectSet<Int>()
        val size = 100
        for (x in 0..size) {
            //println("hashCodeTest, x: $x")
            for (y in 0..size) {
                for (z in 0..size) {
                    //val cdg = CubeDataGrid()
                    //cdg.init(x.toFloat(), y.toFloat(), z.toFloat())
                    //val hash = cdg.hashCode()
                    val hash = threeIntegerHashCode(x, y, z)
                    assertFalse("duplicate hash codes must not exist for CubeDataGrid\n(xyz: $x, $y, $z) hash: $hash",
                            set.contains(hash))
                    set.add(hash)
                }
            }
        }
    }

    test fun iteration() {
        // test multiple times to test reset() method
        val cdg = basicCDG()
        for (n in 1..3) {
            var count = 0
            for (item in cdg) {
                count++
            }
            assertEquals(expectedNumElements, count)
        }
    }

    /** test iteration order of Y, then X, then Z */
    test fun iterationOrder() {
        val cdg = basicCDG()
        val grid = cdg.grid
        val manualCollection = Arr<CubeData>()
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                for (z in grid[y][x].indices) {
                    val cubeData = grid[y][x][z]
                    manualCollection.add(cubeData)
                }
            }
        }
        val iteratorCollection = Arr<CubeData>()
        for (cubeData in cdg) {
            iteratorCollection.add(cubeData)
        }
        assertEquals(manualCollection.size, iteratorCollection.size)
        val manualPos = Vector3()
        val iteratorPos = Vector3()
        for (i in 0..manualCollection.size - 1) {
            manualPos set manualCollection.get(i).getPositionTempVec()
            iteratorPos set iteratorCollection.get(i).getPositionTempVec()
            assertEquals(manualPos, iteratorPos)
        }
    }

    test fun hasCube() {
        val width = CubeDataGrid.width
        val height = CubeDataGrid.height
        val depth = CubeDataGrid.depth

        val cdg = basicCDG()
        val under = 0.99f
        for (x in 0..width-1) {
            for (y in 0..height-1) {
                for (z in 0..depth-1) {
                    assertTrue(cdg.hasCubeAt(x.toFloat(), y.toFloat(), z.toFloat()))
                    assertTrue(cdg.hasCubeAt(x + under, y + under, z + under))
                }
            }
        }
        assertTrue(cdg.hasCubeAt(0f, 0f, 0f))
        assertFalse(cdg.hasCubeAt(-0.01f, -0.01f, -0.01f))
        assertFalse(cdg.hasCubeAt(-0.01f, 1f, 1f))
        assertFalse(cdg.hasCubeAt(width.toFloat(), height.toFloat(), depth.toFloat()))
        assertFalse(cdg.hasCubeAt(width + 0.01f, height + 0.01f, depth + 0.01f))
    }

    test fun getNeighbor() {
        val width = CubeDataGrid.width
        val height = CubeDataGrid.height
        val depth = CubeDataGrid.depth
        val origin = Vector3(50f, 25f, 12f)
        val grid = CubeDataGrid.create(origin.x, origin.y, origin.z)
        assertTrue(grid.hasCubeAt(origin))
        assertTrue(grid.hasCubeAt(origin.x + width-1, origin.y + height-1, origin.z + depth-1))

        assertFalse(grid.hasCubeAt(-100f, -100f, -100f))
        assertFalse(grid.hasCubeAt(0f, 0f, 0f))
        assertFalse(grid.hasCubeAt(origin.x + width, origin.y + height, origin.z + depth))
        assertFalse(grid.hasCubeAt(origin.x - 1, origin.y - 1, origin.z - 1))

        val iv = IntVector3().set(origin)

        for (x in iv.x..iv.x+width-1) {
            for (y in iv.y..iv.y+height-1) {
                for (z in iv.z..iv.z+depth-1) {
                    assertTrue(grid.hasCubeAt(x.toFloat(), y.toFloat(), z.toFloat()))
                    assertNotNull(grid.getCubeAt(x.toFloat(), y.toFloat(), z.toFloat()))
                }
            }
        }
    }

    test fun chunkElevation() {
        val cdg = basicCDG()
        val maxElevation = 5
        for (cube in cdg) {
            if(cube.y > maxElevation) {
                cube.cubeType = CubeType.Void
            } else {
                cube.cubeType = CubeType.Grass
            }
        }
        fun CubeDataGrid.test(x: Float, z: Float, expected: Int) {
            assertEquals(expected, this.getChunkLocalElevation(x, z))
        }
        val sz = world.Chunk.chunkSize
        println("chunk size: $sz")
        cdg.test(0f, 0f, maxElevation)
        cdg.test(0f, sz-1f, maxElevation)
        cdg.test(sz-1f, 0f, maxElevation)
        cdg.test(sz/2f, sz/2f, maxElevation)
        for (x in 0..sz-1) {
            for (z in 0..sz-1) {
                cdg.test(x.toFloat(), z.toFloat(), expected = maxElevation)
            }
        }

        val cdg2 = basicCDG()
        cdg2 forEach { it.cubeType = CubeType.Grass }
        cdg2.getCubeAt(0f, 5f, 0f).cubeType = CubeType.Void
        cdg2.test(0f, 0f, expected = sz-1)
        cdg2.getCubeAt(0f, 3f, 0f).cubeType = CubeType.Void
        cdg2.test(0f, 0f, expected = sz-1)

    }
}

class WorldTest {

    fun createWorld(w: Int, h: Int, d: Int): World {
        val w = World(w, h, d)
        world = w
        w.updatesEnabled = false
        w.createAllChunks()
        return w
    }

    test fun chunkDivisionCubeCount() {
        fun testCubeCount(szMult: Int) {
            val sz = szMult * learngl.world.Chunk.chunkSize
            if (sz % learngl.world.Chunk.chunkSize != 0) {
                throw GdxRuntimeException("Invalid world size, world size must be divisible by chunk size")
            }
            val world = createWorld(sz, sz, sz)
            val expectedTotalCubes = sz * sz * sz
            val totalCubes = world.chunks.fold(0, {(value, chunk) -> value + chunk.dataGrid.numElements })
            assertEquals(expectedTotalCubes, totalCubes)
        }
        for (worldSize in 1..4) {
            testCubeCount(worldSize)
        }
    }

    test fun chunkDivisionChunkCount() {
        fun assertCounts(world: World, xCount: Int, yCount: Int, zCount: Int) {
            //println("world: ${world.width}, ${world.height}, ${world.depth}")
            assertEquals(world.numChunksX, xCount)
            assertEquals(world.numChunksY, yCount)
            assertEquals(world.numChunksZ, zCount)
        }

        val sz = world.Chunk.chunkSize

        var w = createWorld(sz, sz, sz)
        assertCounts(w, 1, 1, 1)

        w = createWorld(sz*2, sz, sz)
        assertCounts(w, 2, 1, 1)
        w = createWorld(sz, sz*2, sz)
        assertCounts(w, 1, 2, 1)
        w = createWorld(sz, sz, sz*2)
        assertCounts(w, 1, 1, 2)

        w = createWorld(sz*10, sz*5, sz*3)
        assertCounts(w, 10, 5, 3)

    }

    test fun crossChunkGetNeighbor() {
        val sz = learngl.world.Chunk.chunkSize
        var world = createWorld(sz, sz, sz)

        fun testNeighbor(x: Float, y: Float, z: Float) {
            assertTrue(world.hasCubeAt(x, y, z))
            assertNotNull(world.getCubeAt(x, y, z))
        }
        testNeighbor(0f, 0f, 0f)
        testNeighbor(0.1f, 0.1f, 0.1f)
        testNeighbor(7f, 7f, 7f)
        val lo = sz-0.01f
        testNeighbor(lo, lo, lo)
        assertFalse(world.hasCubeAt(sz+0.1f, 0f, 0f))
        assertFalse(world.hasCubeAt(0f, sz+0.1f, 0f))
        assertFalse(world.hasCubeAt(0f, 0f, sz+0.1f))
        assertFalse(world.hasCubeAt(-0.1f, -0.1f, -0.1f))

        world = createWorld(17, 17, 17)
        for (y in 0..16) {
            for (x in 0..16) {
                for (z in 0..16) {
                    testNeighbor(x.toFloat(), y.toFloat(), z.toFloat())
                }
            }
        }
    }

    test fun hiddenFaces() {
        fun createTestWorld(w: Int, h: Int, d: Int): World {
            val wor = createWorld(w, h, d)
            wor.calculateHiddenFaces()
            return wor
        }
        fun World.testHiddenFaces(x: Float, y: Float, z: Float, expected: Int) {
            val cube = this.getCubeAt(x, y, z)
            //println("CUBE AT ${cube.getPositionTempVec()}")
            //println(cube.debugHiddenFaces)
            assertEquals(expected, this.getCubeAt(x, y, z).hiddenFacesCount)
        }
        val sz = 32
        val max = sz-1.toFloat()
        val testWorld = createTestWorld(sz, sz, sz)
        //println("world chunks: ${testWorld.chunks.size}")
        var total = 0
        //print("hidden faces per chunk: ")
        for (chunk in testWorld.chunks) {
            chunk.dataGrid.calculateHiddenFaces(testWorld)
            var subTotal = chunk.dataGrid.numberOfHiddenFaces()
            //print(", $subTotal")
            total += subTotal
        }
        //println("\ntotal hidden faces in world: $total")
        testWorld.testHiddenFaces(0f, 0f, 0f, expected = 3)
        testWorld.testHiddenFaces(1f, 0f, 0f, expected = 4)
        testWorld.testHiddenFaces(0f, 1f, 0f, expected = 4)
        testWorld.testHiddenFaces(0f, 0f, 1f, expected = 4)
        testWorld.testHiddenFaces(1f, 1f, 0f, expected = 5)
        testWorld.testHiddenFaces(1f, 1f, 1f, expected = 6)

        testWorld.testHiddenFaces(max-1, max-1, max-1, expected = 6)
        testWorld.testHiddenFaces(max, max, max, expected = 3)

        val world2 = createTestWorld(128, 16, 16)
        world2.testHiddenFaces(0f, 0f, 0f, expected = 3)
        world2.testHiddenFaces(127.5f, 0f, 0f, expected = 3)
        world2.testHiddenFaces(127.5f, 15.5f, 15.5f, expected = 3)
        world2.testHiddenFaces(15.5f, 15.5f, 15.5f, expected = 3)
        world2.testHiddenFaces(15.0f, 15.0f, 8.0f, expected = 4)
    }

    test fun testChunkSnapping() {
        // world size is actually irrelvant to this test, see world.snapToChunkCenter method contents for details
        val w = createWorld(8, 8, 8)
        val v = Vector3()
        val sz = world.Chunk.chunkSize
        val szf = sz.toFloat()
        val origin = Vector3()
        val delta = 0.00001f
        fun test() {
            assertEquals(origin.x, w.snapToChunkOrigin(v.x), delta)
            assertEquals(origin.y, w.snapToChunkOrigin(v.y), delta)
            assertEquals(origin.z, w.snapToChunkOrigin(v.z), delta)

            val centerOffset = world.Chunk.chunkSize / 2f
            assertEquals(centerOffset + origin.x, w.snapToChunkCenter(v.x), delta)
            assertEquals(centerOffset + origin.y, w.snapToChunkCenter(v.y), delta)
            assertEquals(centerOffset + origin.z, w.snapToChunkCenter(v.z), delta)
        }

        if (sz < 4) {
            throw GdxRuntimeException("this test depends on chunk size being >= 8")
        }
        origin.set(0f, 0f, 0f)
        v.set(1.12f, 2.98f, 3.84f)
        test()

        origin.set(0f, 0f, 0f)
        v.set(sz-0.1f, sz-0.1f, sz-0.1f)
        test()

        origin.set(szf, szf, szf)
        v.set(szf, szf, szf)
        test()

        origin.set(szf, szf, szf)
        v.set(szf+0.01f, szf+0.01f, szf+0.01f)
        test()

        origin.set(0f, 0f, 0f)
        v.set(szf-0.01f, szf-0.01f, szf-0.01f)
        test()

        origin.set(128f, 128f, 128f)
        v.set(129f, 129f, 130f)
        test()
    }

    test fun hasCube_and_hasChunk() {
        val sz = 32
        val w = createWorld(sz, sz, sz)
        val max = sz-1
        fun testHas(x: Float, y: Float, z: Float) {
            assertTrue("xyz: $x, $y, $z", w.hasChunkAt(x, y, z))
            assertTrue("xyz: $x, $y, $z", w.hasCubeAt(x, y, z))
        }
        fun testHasNot(x: Float, y: Float, z: Float) {
            assertFalse("xyz: $x, $y, $z", w.hasChunkAt(x, y, z))
            assertFalse("xyz: $x, $y, $z", w.hasCubeAt(x, y, z))
        }
        for (xi in 0..max) {
            for (yi in 0..max) {
                for (zi in 0..max) {
                    val x = xi.toFloat()
                    val y = yi.toFloat()
                    val z = zi.toFloat()
                    testHas(x, y, z)
                }
            }
        }
        testHas(0f, 0f, 0f)
        testHasNot(-0.01f, -0.01f, -0.01f)
        testHas(0.05f, 0.05f, 0.05f)
        testHasNot(max + 1.1f, max + 1.1f, max + 1.1f)
        val fsz = sz.toFloat()
        testHasNot(fsz, fsz, fsz)
    }
    
    test fun getCube() {
        val sz = 32
        val max = sz-1
        val w = createWorld(sz, sz, sz)
        val tmp = Vector3()
        fun Float.i(): Int = MathUtils.floor(this)
        // snap float values to integer before comparing
        fun equivalentAsIntegers(v1: Vector3, v2: Vector3) {
            assertTrue(v1.x.i() == v2.x.i())
            assertTrue(v1.y.i() == v2.y.i())
            assertTrue(v1.z.i() == v2.z.i())
        }
        fun test(x: Float, y: Float, z: Float) {
            val cube = w.getCubeAt(x, y, z)
            tmp.set(x, y, z)
            equivalentAsIntegers(tmp, cube.getPositionTempVec())
        }
        for (x in 0..max) {
            for (y in 0..max) {
                for (z in 0..max) {
                    test(x + 0f, y + 0f, z + 0f)
                    test(x + 0.95f, y + 0.95f, z + 0.95f)
                    test(x + 0.01f, y + 0.01f, z + 0.01f)
                }
            }
        }
    }

    test fun getChunk() {
        val sz = 32
        val max = sz-1
        val w = createWorld(sz, sz, sz)

        fun test(x: Float, y: Float, z: Float) {
            val chunk = w.getChunkAt(x, y, z)
            val worldCube = w.getCubeAt(x, y, z)
            val chunkCube = chunk.dataGrid.getCubeAt(x, y, z)
            assertEquals(worldCube, chunkCube)
            assertTrue(worldCube.identityEquals(chunkCube))
        }

        for (x in 0..max) {
            for (y in 0..max) {
                for (z in 0..max) {
                    test(x + 0f, y + 0f, z + 0f)
                    test(x + 0.95f, y + 0.95f, z + 0.95f)
                    test(x + 0.01f, y + 0.01f, z + 0.01f)
                }
            }
        }
    }
}
