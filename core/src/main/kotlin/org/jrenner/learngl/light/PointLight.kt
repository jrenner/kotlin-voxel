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

class PointLight {

    val pos = Vector3()
    val color = Color(Color.WHITE)
    var intensity = 5.0f

    val accel = 0.05f
    val maxSpeed = r(0.2f, 0.5f)
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

    var dest = Vector3()
    ;{
        setNextDest()
    }

    val xOffset = -2f

    fun setNextDest() {
        //dest.set(xOffset, r(world.height.toFloat()), r(world.depth.toFloat()))
        val cam = view.camera.position
        val limit = 40f
        dest.y = cam.y + r(-2f, 2f)
        dest.x = r(cam.x - limit, cam.x + limit)
        dest.z = r(cam.z - limit, cam.z + limit)
    }

    var posLoc = -1
    var colorLoc = -1
    var intensityLoc = -1

    var attachedToCamera = false

    val tmp = Vector3()

    fun update() {
        if (view.debug) {
            debugInstance.transform.setToTranslation(pos)
        }
        if (attachedToCamera) {
            pos.set(view.camera.position)
            return
        }
        val diff = tmp.set(dest).sub(pos)
        val dist = diff.len()
        velocity.scl(0.9f)
        if (dist < velocity.len() * 2f) {
            setNextDest()
        } else {
            velocity.add(diff.nor().scl(accel)).clamp(0f, maxSpeed)
        }
        pos.add(velocity)
    }

    fun setUniforms() {
        val shader = view.shader
        shader.setUniformf(posLoc, pos.x, pos.y, pos.z)
        shader.setUniformf(colorLoc, color.r, color.g, color.b)
        shader.setUniformf(intensityLoc, intensity)
    }
}