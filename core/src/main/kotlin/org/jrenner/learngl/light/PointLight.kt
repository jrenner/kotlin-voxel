package org.jrenner.learngl.light

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.graphics.Color
import kotlin.properties.Delegates
import org.jrenner.learngl.view
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.Model
import org.jrenner.learngl.utils.r
import org.jrenner.learngl.world
import org.jrenner.learngl.TimedIntervalTask
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import org.jrenner.learngl.lights
import org.jrenner.learngl.utils.fmt

class PointLight {

    val pos = Vector3()
    val color = Color(Color.WHITE)
    var intensity = 5.0f

    val accel = 0.005f
    val maxSpeed = 2.0f
    val velocity = Vector3()

    val debugModel: Model by Delegates.lazy {
        val mb = ModelBuilder()
        val sz = 0.5f
        val div = 12
        val mat = Material(ColorAttribute.createDiffuse(color))
        val attr = (Usage.Position or Usage.ColorPacked).toLong()
        mb.createSphere(sz, sz, sz, div, div, mat, attr)
    }

    val debugInstance: ModelInstance by Delegates.lazy {
        ModelInstance(debugModel)
    }

    var destX = 0f
    var destZ = 0f
    init {
        setNextDest()
    }

    val xOffset = -2f

    fun setNextDest() {
        //dest.set(xOffset, r(world.height.toFloat()), r(world.depth.toFloat()))
        val cam = view.camera.position
        val limit = 80f
        destX = r(cam.x - limit, cam.x + limit)
        destZ = r(cam.z - limit, cam.z + limit)
    }

    var posLoc = -1
    var colorLoc = -1
    var intensityLoc = -1

    var attachedToCamera = false

    val tmp = Vector3()
    var elevation = 0f
    var elevationOffset = r(5f, 20f)

    val elevationUpdater = TimedIntervalTask(intervalSeconds = 0.25f) {
        elevation = world.getElevation(pos.x, pos.z).toFloat()
    }

    fun update(dt: Float) {
        elevationUpdater.update(dt)
        if (view.debug) {
            debugInstance.transform.setToTranslation(pos)
        }
        if (attachedToCamera) {
            pos.set(view.camera.position)
            return
        }
        val diff = tmp.set(destX, 0f, destZ).sub(pos.x, 0f, pos.z)
        val dist = diff.len()
        velocity.y = 0f
        if (dist < 1f) {
            setNextDest()
        } else {
            velocity.add(diff.nor().scl(accel))
        }
        velocity.scl(0.98f)
        pos.add(velocity.x, 0f, velocity.z)
        pos.y = MathUtils.lerp(pos.y, elevation + elevationOffset, 0.05f)
    }

    fun setUniforms() {
        val shader = view.shader
        shader.setUniformf(posLoc, pos.x, pos.y, pos.z)
        shader.setUniformf(colorLoc, color.r, color.g, color.b)
        shader.setUniformf(intensityLoc, intensity)
    }
}