package org.jrenner.learngl.light
import com.badlogic.gdx.utils.Array as Arr
import org.jrenner.learngl.view
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.GdxRuntimeException
import org.jrenner.learngl.utils.randomizeColor

class Lights {
    val pointLights = Arr<PointLight>()
    val dirLight = DirectionalLight()
    val uniformAmbient = 0f
    val ambientLight = Color(uniformAmbient, uniformAmbient, uniformAmbient, 1.0f)
    var ambientLoc = -1

    fun update(dt: Float) {
        for (pl in pointLights) {
            pl.update(dt)
        }
    }

    fun setLocations() {
        val shader = view.shader
        ambientLoc = shader.getUniformLocation("u_ambientLight")
        if (ambientLoc == -1) throw GdxRuntimeException("couldn't get location for u_ambientLight from shader")
        for (i in 0..pointLights.size - 1) {
            val pl = pointLights[i]
            pl.posLoc = shader.getUniformLocation("u_pointLights[$i].pos")
            pl.colorLoc = shader.getUniformLocation("u_pointLights[$i].color")
            pl.intensityLoc = shader.getUniformLocation("u_pointLights[$i].intensity")
            //println("PointLight locations, pos: ${pl.posLoc}, color: ${pl.colorLoc}")
            if (pl.posLoc == -1 || pl.colorLoc == -1 || pl.intensityLoc == -1) throw GdxRuntimeException("bad shader location(s) for PointLight")
        }
        dirLight.colorLoc = shader.getUniformLocation("u_dirLight.color")
        dirLight.directionLoc = shader.getUniformLocation("u_dirLight.direction")
        if (dirLight.colorLoc == -1 || dirLight.directionLoc == -1) throw GdxRuntimeException("bad shader location(s), dirlight")
    }

    fun setUniforms() {
        view.shader.setUniformf(ambientLoc, ambientLight.r, ambientLight.g, ambientLight.b, ambientLight.a)
        for (pl in pointLights) {
            pl.setUniforms()
        }
        dirLight.setUniforms()
    }

    // CREATE LIGHTS
    init {
        val camLight = PointLight()
        pointLights.add(camLight)
        //pl.pos.set(0f, 0f, 0f)
        camLight.intensity = 10f
        camLight.attachedToCamera = true

        val intensity = 0.15f
        dirLight.color.set(intensity, intensity, intensity, 1.0f)
        dirLight.direction.set(-.5f, -1f, -.5f).nor()

        for (n in 1..10) {
            val pl = PointLight()
            randomizeColor(pl.color, min = 0.3f, max = 1.0f)
            pointLights.add(pl)
        }

        setLocations()
    }
}