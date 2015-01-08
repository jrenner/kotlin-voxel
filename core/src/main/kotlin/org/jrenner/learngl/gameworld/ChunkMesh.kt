package org.jrenner.learngl.gameworld

import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.GdxRuntimeException
import org.jrenner.learngl.cube.CubeDataGrid
import com.badlogic.gdx.math.Vector2
import kotlin.properties.Delegates
import org.jrenner.learngl.Direction
import org.jrenner.learngl

class ChunkMesh() {
    class object {
        private val cubeRectData = RectData()

        private val POSITION_COMPONENTS = 3
        //private val COLOR_COMPONENTS = 4
        private val TEXTURE_COORDS = 2
        private val NORMAL_COMPONENTS = 3
        private val NUM_COMPONENTS = POSITION_COMPONENTS + TEXTURE_COORDS + NORMAL_COMPONENTS

        private val VERTS_PER_TRI = 3
        private val TRIS_PER_FACE = 2
        private val FACES_PER_CUBE = 6
        private val CUBE_SIZE = 1f
        val MAX_VERTS = VERTS_PER_TRI * TRIS_PER_FACE * FACES_PER_CUBE * (Chunk.chunkSize * Chunk.chunkSize * Chunk.chunkSize) * NUM_COMPONENTS

        val verts = FloatArray(MAX_VERTS)
    }

    //private val PRIMITIVE_SIZE = 3 * NUM_COMPONENTS

    private var cdg: CubeDataGrid by Delegates.notNull()

    var numCubes = 0
    var numFaces = 0
    var numTris = 0
    var numVerts = 0
    var numFloats = 0

    fun reset(cdg: CubeDataGrid) {
        this.cdg = cdg
        numCubes = cdg.numberOfNonVoidCubes()
        numFaces = FACES_PER_CUBE * numCubes - cdg.numberOfHiddenFaces()
        numTris = numFaces * TRIS_PER_FACE
        numVerts = numTris * VERTS_PER_TRI
        numFloats = numVerts * NUM_COMPONENTS
    }

    private var cubesCreated = 0
    private var idx = 0
    private var triangles = 0
    var vertexCount = 0

    var hasMesh = false

    fun resetMesh() {
        if (hasMesh) {
            mesh.dispose()
        }
        mesh = Mesh(true, numVerts, 0,
                VertexAttribute(Usage.Position, POSITION_COMPONENTS, "a_position"),
                //VertexAttribute(Usage.ColorUnpacked, COLOR_COMPONENTS, "a_color"),
                VertexAttribute(Usage.TextureCoordinates, TEXTURE_COORDS, "a_textureCoords"),
                VertexAttribute(Usage.Normal, NORMAL_COMPONENTS, "a_normal"))
        hasMesh = true
    }

    var mesh: Mesh by Delegates.notNull()

    // EXPERIMENTAL: re-use the same mesh to avoid diposal peformance cost
    // the downside is that every mesh will be of max size = MAX_VERTS, ballooning native memory
    /*val mesh = Mesh(true, MAX_VERTS, 0,
            VertexAttribute(Usage.Position, POSITION_COMPONENTS, "a_position"),
            //VertexAttribute(Usage.ColorUnpacked, COLOR_COMPONENTS, "a_color"),
            VertexAttribute(Usage.TextureCoordinates, TEXTURE_COORDS, "a_textureCoords"),
            VertexAttribute(Usage.Normal, NORMAL_COMPONENTS, "a_normal"))

    fun resetMesh() {
        // do nothing
    }*/

    var started = false

    private fun start() {
        if (started) throw GdxRuntimeException("call end() first!")
        started = true
        idx = 0
        triangles = 0
        cubesCreated = 0
        vertexCount = 0
    }

    fun buildMesh() {
        resetMesh()
        start()
        for (cubeData in cdg) {
            //println("create cube at: ${cubeData.position.fmt}")
            //println("cube: ${cubeData.cubeType}")
            if (cubeData.cubeType != CubeType.Void) {
                addCube(cubeData.getPositionTempVec(), CUBE_SIZE, cubeData.hiddenFaces)
            }
        }
        end()
    }


    private fun end() {
        if (!started) throw GdxRuntimeException("call start() first!")
/*        if (cubesCreated != NUM_CUBES) {
            throw GdxRuntimeException("cubes created (${cubesCreated}) is not equal to NUM_CUBES (${NUM_CUBES})")
        }*/
/*        if (vertexCount != NUM_VERTS) {
            throw GdxRuntimeException("vertexCount ($vertexCount) != NUM_VERTS ($NUM_VERTS)")
        }*/
        started = false
        mesh.setVertices(verts, 0, numFloats);
        //mesh.updateVertices(0, verts, 0, numFloats)
    }

    private fun addRect(rv: RectData) {
        triangle(rv.v00, rv.v10, rv.v11, rv.normal, rv.uv00, rv.uv10, rv.uv11)
        triangle(rv.v00, rv.v11, rv.v01, rv.normal, rv.uv00, rv.uv11, rv.uv01)
    }

    private fun addCube(origin: Vector3, sz: Float, hiddenBitwise: Int) {
        cubesCreated++
        val r = cubeRectData

        val n = 1f

        r.uv00.set(0f, n)
        r.uv10.set(n, n)
        r.uv11.set(n, 0f)
        r.uv01.set(0f, 0f)

        /*var hiddenFaces = 0
        for (dir in array(NORTH, SOUTH, EAST, WEST, UP, DOWN)) {
            if (hiddenBitwise and dir != 0) {
                hiddenFaces++
            }
        }
        println("hidden faces for cube: $hiddenFaces")*/

        if (hiddenBitwise and Direction.North == 0) {
            r.v00.set(0f, 0f, sz).add(origin)
            r.v10.set(sz, 0f, sz).add(origin)
            r.v11.set(sz, sz, sz).add(origin)
            r.v01.set(0f, sz, sz).add(origin)
            r.normal.set(0f, 0f, 1f)
            addRect(r)
        }

        if (hiddenBitwise and Direction.South == 0) {
            r.v00.set(sz, 0f, 0f).add(origin)
            r.v10.set(0f, 0f, 0f).add(origin)
            r.v11.set(0f, sz, 0f).add(origin)
            r.v01.set(sz, sz, 0f).add(origin)
            r.normal.set(0f, 0f, -1f)
            addRect(r)
        }

        if (hiddenBitwise and Direction.Down == 0) {
            r.v00.set(0f, 0f, 0f).add(origin)
            r.v10.set(sz, 0f, 0f).add(origin)
            r.v11.set(sz, 0f, sz).add(origin)
            r.v01.set(0f, 0f, sz).add(origin)
            r.normal.set(0f, -1f, 0f)
            addRect(r)
        }

        if (hiddenBitwise and Direction.Up == 0) {
            r.v00.set(sz, sz, 0f).add(origin)
            r.v10.set(0f, sz, 0f).add(origin)
            r.v11.set(0f, sz, sz).add(origin)
            r.v01.set(sz, sz, sz).add(origin)
            r.normal.set(0f, 1f, 0f)
            addRect(r)
        }


        if (hiddenBitwise and Direction.West == 0) {
            r.v00.set(0f, 0f, 0f).add(origin)
            r.v10.set(0f, 0f, sz).add(origin)
            r.v11.set(0f, sz, sz).add(origin)
            r.v01.set(0f, sz, 0f).add(origin)
            r.normal.set(-1f, 0f, 0f)
            addRect(r)
        }

        if (hiddenBitwise and Direction.East == 0) {
            r.v00.set(sz, 0f, sz).add(origin)
            r.v10.set(sz, 0f, 0f).add(origin)
            r.v11.set(sz, sz, 0f).add(origin)
            r.v01.set(sz, sz, sz).add(origin)
            r.normal.set(1f, 0f, 0f)
            addRect(r)
        }
    }

    private fun triangle(a: Vector3, b: Vector3, c: Vector3, nor: Vector3, uvA: Vector2, uvB: Vector2, uvC: Vector2) {
        triangles++
        vertex(a, nor, uvA)
        vertex(b, nor, uvB)
        vertex(c, nor, uvC)
    }

    private fun vertex(v: Vector3, nor: Vector3, uv: Vector2) {
        vertexCount++

        // POSITION
        verts[idx++] = v.x
        verts[idx++] = v.y
        verts[idx++] = v.z

        // TEXTURE_UV
        verts[idx++] = uv.x
        verts[idx++] = uv.y

        // NORMAL
        verts[idx++] = nor.x
        verts[idx++] = nor.y
        verts[idx++] = nor.z
    }

    fun dispose() {
        mesh.dispose()
    }
}

class RectData() {
    val v00 = Vector3()
    val v10 = Vector3()
    val v01 = Vector3()
    val v11 = Vector3()
    val normal = Vector3()
    val uv00 = Vector2()
    val uv10 = Vector2()
    val uv01 = Vector2()
    val uv11 = Vector2()
}
