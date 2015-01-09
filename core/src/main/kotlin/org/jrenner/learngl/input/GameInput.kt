package org.jrenner.learngl.input

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import kotlin.properties.Delegates
import org.jrenner.learngl
import com.badlogic.gdx.Input
import org.jrenner.learngl.View
import com.badlogic.gdx.Input.Keys
import org.jrenner.learngl.view

class GameInput {
    val input = Gdx.input!!
    val multi = InputMultiplexer()
    var proc: InputAdapter by Delegates.notNull()
    val camControl: FirstPersonCameraController get() = learngl.view.camControl
    ;
    {
        proc = createMainProcessor()
        resetProcessors()
    }

    fun resetProcessors() {
        multi.clear()
        input.setInputProcessor(multi)
        multi.addProcessor(proc)
        multi.addProcessor(camControl)
    }

    fun update(dt: Float) {
        camControl.update(dt)
    }

    fun createMainProcessor(): InputAdapter {
        return object : InputAdapter() {

            override fun keyDown(keycode: Int): Boolean {
                when (keycode) {
                    Keys.G -> learngl.main.profileRequested = true // profile OpenGL
                    Keys.V -> {
                        learngl.main.resetViewRequested = true
                    }
                    /*Keys.H -> {
                        learngl.hud.enabled = !learngl.hud.enabled
                        println("HUD enabled: ${learngl.hud.enabled}")
                    }*/
/*                    Keys.F -> {
                        learngl.hiddenFacesEnabled = !learngl.hiddenFacesEnabled
                        println("hideFaces: $org.jrenner.learngl.hiddenFacesEnabled")
                    }*/
                    Keys.PLUS -> {
                        View.maxViewDist += 10f
                        println("MAX VIEW DIST: ${View.maxViewDist}")
                    }
                    Keys.MINUS -> {
                        View.maxViewDist -= 10f
                        println("MAX VIEW DIST: ${View.maxViewDist}")
                    }
                    Keys.SPACE -> {
                        view.walkingEnabled = !view.walkingEnabled
                        println("walking mode: ${view.walkingEnabled}")
                        when (view.walkingEnabled) {
                            true -> view.camControl.setVelocity(View.WALKING_MAX_VELOCITY)
                            false -> view.camControl.setVelocity(View.FLYING_MAX_VELOCITY)
                        }

                    }
                    else -> Unit
                }
                return false
            }
        }
    }
}