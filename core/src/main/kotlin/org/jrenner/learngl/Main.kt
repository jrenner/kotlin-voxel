package org.jrenner.learngl

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.FPSLogger
import kotlin.properties.Delegates
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array as Arr
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.graphics.Color
import org.jrenner.learngl.gameworld.Chunk
import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.light.Lights
import org.jrenner.learngl.cube.CubeDataGrid
import org.jrenner.learngl.input.GameInput
import org.jrenner.learngl.gameworld.World

class Main : ApplicationAdapter() {
    companion object {
        public val mainWorld: World get() = world // for easier Java interop
        public val mainChunkPool: DebugPool<Chunk> get() = chunkPool
        public val mainCDGPool: DebugPool<CubeDataGrid> get() = cdgPool

    }

    val fpsLogger = FPSLogger()


    override fun create() {
        main = this
        fonts = Fonts()
        assets = Assets()
        assets.load()
        view = View()
        hud = HUD()
        gameInput = GameInput()
        GLProfiler.enable()
        world = World(8192, 64, 8192)
        view.camera.position.set(world.width / 2f, 50f, world.depth / 2f)
        view.camera.lookAt(world.width.toFloat(), 0f, world.depth / 2f)
        println("world size: ${world.width * world.height * world.depth}")
        lights = Lights()
    }

    val viewResetter = TimedIntervalTask(1.0f, {
        resetViewRequested = true
    })

    override fun render() {
        /*if (frame % 120 == 0L) {
            //println(DebugPool.allDebugInfo())
            //world.elevTimer.report()
        }*/
        FramePool.reset()
        if (resetViewRequested) {
            resetViewRequested = false
            val pos = Vector3(view.camera.position)
            val direction = Vector3(view.camera.direction)
            fonts.dispose()
            fonts = Fonts()
            assets.load()
            view = View()
            hud = HUD()
            lights = Lights()
            gameInput.resetProcessors()
            view.camera.position.set(pos)
            view.camera.direction.set(direction)
            for (light in lights.pointLights) {
                light.pos.set(view.camera.position)
                light.setNextDest()
            }
            println("view reset")
        }
        val dt = Gdx.graphics.getDeltaTime()
        frameTimes[frameTimeIdx++] = dt
        world.update(dt)
        frame++
        profileGL()
        //fpsLogger.log()
        view.render(dt)
        hud.render(dt)
        gameInput.update(dt)
        /*if (frame % 120 == 0L) {
            resetViewRequested = true
        }*/
        Physics.update()
    }

    var profileRequested = false
    var resetViewRequested = false

    fun profileGL() {
        if (profileRequested) {
            profileRequested = false
            println("calls: ${GLProfiler.calls}")
            println("draw calls: ${GLProfiler.drawCalls}")
            val min = GLProfiler.vertexCount.min
            val max = GLProfiler.vertexCount.max
            val avg = GLProfiler.vertexCount.average
            val total = GLProfiler.vertexCount.total
            println("vertices: min,boundary: $min, $max - average: $avg - total: $total")
            println("----------------------------------------------")
        }
        GLProfiler.reset()
    }

    override fun resize(width: Int, height: Int) {
        //super.resize(width, height)
        resetViewRequested = true
    }
}

var fonts: Fonts by Delegates.notNull()
var main: Main by Delegates.notNull()
var view: View by Delegates.notNull()
var hud: HUD by Delegates.notNull()
var gameInput: GameInput by Delegates.notNull()
var skin: Skin by Delegates.notNull()
var assets: Assets by Delegates.notNull()
var world: World by Delegates.notNull()
var lights: Lights by Delegates.notNull()

var frame = 0L

val screenWidth: Int get() = Gdx.graphics.getWidth()
val screenHeight: Int get() = Gdx.graphics.getHeight()
val screenRatio: Float get() = screenWidth / 1080f

val startTime = TimeUtils.millis()
val millisSinceStart: Long get() = TimeUtils.millis() - startTime
val secondsSinceStart: Float get() = millisSinceStart / 1000f
var lastSecond = 0f

var hiddenFacesEnabled = true

val initialChunkPoolSize = 2048
val chunkPool = DebugPool("Chunk", { Chunk() },  initialChunkPoolSize)
val cdgPool = DebugPool("CDG", { CubeDataGrid() }, initialChunkPoolSize)

fun rand(f: Float): Float {
    return MathUtils.random(f)
}

fun randColor(): Color {
    return Color(rand(1f), rand(1f), rand(1f), 1f)
}

val frameTimes : FloatArray by lazy { FloatArray(screenHeight) }
var frameTimeIdx: Int = 0
    get() {
        if (field >= frameTimes.size) {
            field = 0
            for (i in 0..frameTimes.size - 1) {
                frameTimes[i] = 0f
            }
        }
        return field
    }