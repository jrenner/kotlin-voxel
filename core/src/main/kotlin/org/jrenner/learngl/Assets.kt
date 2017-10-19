package org.jrenner.learngl

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.graphics.Texture
import kotlin.properties.Delegates
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.Texture.TextureWrap
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter

class Assets {
    val manager = AssetManager()

    private val texturePath = "texture/"

    fun load(){

        val texParam = TextureParameter()
        texParam.genMipMaps = true
        texParam.magFilter = TextureFilter.Linear
        texParam.minFilter = TextureFilter.MipMapLinearLinear

        fun loadTex(path: String) = manager.load(texturePath + path, Texture::class.java, texParam)

        arrayOf(
                "dirt.png"
                //"grass.png"
        ).forEach {
            loadTex(it)
        }

        manager.load("ui/ui.json", Skin::class.java)
        manager.finishLoading()

        skin = manager.get("ui/ui.json")

        setupSkinFonts()

    }

    private fun getTexture(name: String): Texture = manager.get(texturePath + name, Texture::class.java)

    val grassTexture by lazy { getTexture("grass.png") }
    val dirtTexture by lazy { getTexture("dirt.png") }

    fun setupSkinFonts() {
        skin.get(LabelStyle::class.java).font = fonts.normal
    }

    val uvTestTexture: Texture by lazy {
        val sz = 64
        val pixmap = Pixmap(sz, sz, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.GRAY)
        pixmap.fill()
        pixmap.setColor(Color.BLACK)
        pixmap.drawCircle(sz / 2, sz / 2, sz / 2 - 2)
        pixmap.setColor(Color.BLUE)
        val c = sz / 2
        pixmap.drawLine(c, c, c + 8, c)
        pixmap.setColor(Color.MAGENTA)
        pixmap.drawLine(c, c, c, c + 8)
        pixmap.setColor(Color.YELLOW)
        pixmap.drawRectangle(c, c + 8, 3, 3)
        pixmap.setColor(Color.RED)
        pixmap.drawRectangle(c + 8, c, 3, 3)
        val tex = Texture(pixmap, true)
        tex.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear)
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat)
        pixmap.dispose()
        tex
    }

    fun createTexture(baseColor: Color): Texture {
        val sz = 64
        val pixmap = Pixmap(sz, sz, Pixmap.Format.RGBA8888)
        for (y in 0..sz-1) {
            for (x in 0..sz-1) {
                val c = MathUtils.random(0.3f, 0.6f)
                val r = Math.max(0f, baseColor.r - c)
                val g = Math.max(0f, baseColor.g - c)
                val b = Math.max(0f, baseColor.b - c)
                pixmap.setColor(r, g, b, 1.0f)
                pixmap.drawPixel(x, y)
            }
        }
        val tex = Texture(pixmap, true)
        //tex.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Linear)
        tex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)
        tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat)
        pixmap.dispose()
        return tex
    }
}
