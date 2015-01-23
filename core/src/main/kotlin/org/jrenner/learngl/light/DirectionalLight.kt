package org.jrenner.learngl.light

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import org.jrenner.learngl.view

class DirectionalLight {
    val color = Color()
    val direction = Vector3()

    var colorLoc = -1
    var directionLoc = -1

    fun setUniforms() {
        val shader = view.shader
        shader.setUniformf(colorLoc, color.r, color.g, color.b)
        shader.setUniformf(directionLoc, direction.x, direction.y, direction.z)
    }
}

