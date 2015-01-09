package org.jrenner.learngl

import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.utils.Array as Arr
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import kotlin.properties.Delegates
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Matrix4
import org.jrenner.learngl.cube.CubeDataGrid
import com.badlogic.gdx.math.MathUtils
import org.jrenner.learngl.gameworld.Chunk
import com.badlogic.gdx.math.Frustum

class View {
    class object {
        var maxViewDist = 160f
            set(d) {
                $maxViewDist = MathUtils.clamp(d, 20f, 240f)
            }

    }
    val gl = Gdx.gl!!
    val camera = PerspectiveCamera(67f, screenWidth.toFloat(), screenHeight.toFloat())
    val fogColor = Color(0.4f, 0.4f, 0.45f, 1.0f) // alpha is fog intensity
    //val fogColor = Color.valueOf("9CD2FF")
    val camControl: FirstPersonCameraController
    {
        gl.glClearColor(fogColor.r, fogColor.g, fogColor.b, 1.0f)
        camera.near = 0.1f
        camera.far = 500f
        camControl = FirstPersonCameraController(camera)
        camControl.setVelocity(30f)
    }

    // begin debug section
    val modelBatch: ModelBatch by Delegates.lazy {
        ModelBatch()
    }
    // end debug

    val shapes = ShapeRenderer()

    val shader: ShaderProgram
    val projTransLocation: Int
    val diffuseTextureLocation: Int
    val diffuseUVLocation: Int
    val maxViewDistLocation: Int
    val camPosLocation: Int
    val fogColorLocation: Int
    {
        val getShader = { (path: String) -> Gdx.files.local(path)!! }
        val vert = getShader("shader/custom.vertex.glsl")
        val frag = getShader("shader/custom.fragment.glsl")
        shader = ShaderProgram(vert, frag)
        val log = shader.getLog()
        if (!shader.isCompiled()) {
            println("SHADER ERROR:\n$log}")
            throw GdxRuntimeException("SHADER DID NOT COMPILE, SEE SHADER LOG ABOVE")
        } else {
            println("SHADER COMPILED OK:\n$log")
        }
        val loc = { (name: String) -> shader.getUniformLocation(name) }
        projTransLocation = loc("u_projTrans")
        diffuseTextureLocation = loc("u_diffuseTexture")
        diffuseUVLocation = loc("u_diffuseUV")
        maxViewDistLocation = loc("u_maxViewDist")
        camPosLocation = loc("u_cameraPos")
        fogColorLocation = loc("u_fogColor")
    }

    val q = Quaternion()

    val debug = true

    fun render() {
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if(debug) {
            modelBatch.begin(camera)
            for (pl in lights.pointLights) {
                if (!pl.attachedToCamera) {
                    modelBatch.render(pl.debugInstance)
                }
            }
            modelBatch.end()
        }

        gl.glEnable(GL20.GL_DEPTH_TEST)
        gl.glEnable(GL20.GL_BLEND);
        gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnable(GL20.GL_CULL_FACE)
        gl.glCullFace(GL20.GL_BACK)

        //gl.glEnable(GL20.GL_TEXTURE_2D);
        camera.up.set(Vector3.Y)
        camera.update()
        lights.update()

        draw()


        gl.glDisable(GL20.GL_DEPTH_TEST)
        gl.glDisable(GL20.GL_CULL_FACE)

        // we don't need this to be exact, let's optimize
        if (frame % 5 == 0L) {
            synchronized(world) {
                world.updater?.tempCamPos?.set(camera.position)
                world.updater?.tempFrustum?.update(camera.invProjectionView)
                world.updater?.maxDist = View.maxViewDist
            }
        }


    }

    var chunksRendered = 0

    fun draw() {
        try {
            shader.begin()
            lights.setUniforms()
            //assets.grassTexture.bind()
            assets.dirtTexture.bind()
            shader.setUniformMatrix(projTransLocation, camera.combined)
            shader.setUniformi(diffuseTextureLocation, 0)
            // subtract by chunkSize to hide popping in/out of chunks
            shader.setUniformf(maxViewDistLocation, maxViewDist - Chunk.chunkSize)
            shader.setUniformf(camPosLocation, camera.position)
            shader.setUniformf(fogColorLocation, fogColor)

            // Grass
            //assets.grassTexture.bind()
            chunksRendered = 0
            for (chunk in world.chunks) {
                if (chunk.chunkMesh.vertexCount != 0 && chunk.inFrustum()) {
                    chunksRendered++
                    chunk.chunkMesh.mesh.render(shader, GL20.GL_TRIANGLES, 0, chunk.chunkMesh.vertexCount)
                }
            }

            shader.end()
        } catch (e: GdxRuntimeException) {
            e.printStackTrace()
            main.resetViewRequested = true
            println("sleep 3 seconds")
            Thread.sleep(3000)
            return
        }

        drawXYZCoords()
        drawFrameTimes()
    }

    val tmp = Vector3()
    val tmp2 = Vector3()

    fun drawChunkBoundingBoxes() {
        shapes.begin(ShapeType.Line)
        shapes.setProjectionMatrix(camera.combined)
        shapes.setColor(Color.GREEN)
        for (chunk in world.chunks) {
            val o = chunk.dataGrid.origin
            val w = CubeDataGrid.width.toFloat()
            val h = CubeDataGrid.height.toFloat()
            val d = CubeDataGrid.depth.toFloat()
            shapes.box(o.x, o.y, o.z + d, w, h, d)
        }
        shapes.end()
    }

    fun drawXYZCoords() {
        val o = tmp2.set(0f, -10f, 0f)
        val n = 5f
        shapes.setProjectionMatrix(camera.combined)
        shapes.begin(ShapeType.Line)
        // x
        shapes.setColor(Color.BLUE)
        shapes.line(o, tmp.set(n, 0f, 0f).add(o))
        // y
        shapes.setColor(Color.GREEN)
        shapes.line(o, tmp.set(0f, n, 0f).add(o))
        // z
        shapes.setColor(Color.RED)
        shapes.line(o, tmp.set(0f, 0f, n).add(o))
        shapes.end()

    }

    val mtx = Matrix4();
    {
        mtx.setToOrtho2D(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
    }

    fun drawFrameTimes() {
        shapes.setProjectionMatrix(mtx)
        shapes.begin(ShapeType.Line)
        shapes.setColor(Color.GREEN)
        val mult = 300f
        val slow = 1 / 45f
        val verySlow = 1 / 30f
        val tooSlow = 1 / 10f

        for (i in 0..frameTimes.size() - 1) {
            val time = frameTimes[i]
            val col = when {
                time >= tooSlow -> Color.RED
                time >= verySlow -> Color.ORANGE
                time >= slow -> Color.YELLOW
                else -> Color.GREEN
            }
            shapes.setColor(col)
            shapes.line(0f, i.toFloat(), time * mult, i.toFloat())
        }
        shapes.end()
    }

    fun inFrustum(x: Float, y: Float, z: Float, bboxRadius: Float, frustum: Frustum): Boolean {
        val w = bboxRadius
        val h = bboxRadius
        val d = bboxRadius
        return frustum.boundsInFrustum(x, y, z, w, h, d)
    }
}
