package org.jrenner.learngl

import org.jrenner.smartfont.SmartFontGenerator
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import kotlin.properties.Delegates
import com.badlogic.gdx.graphics.g2d.BitmapFont

class Fonts {

    var normal: BitmapFont by Delegates.notNull()

    init {
        val gen = SmartFontGenerator()
        val fileHandle = Gdx.files.local("fonts/Exo-Regular.otf")
        val size: Int = 18
        val fontName = "exo" + size
        println("$fontName")
        val chars = FreeTypeFontGenerator.DEFAULT_CHARS
        normal = gen.createFont(fileHandle, fontName, size, chars)
    }

    fun dispose() {
        normal.dispose()
    }

}
