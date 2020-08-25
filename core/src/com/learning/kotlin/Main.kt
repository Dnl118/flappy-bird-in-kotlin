package com.learning.kotlin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Main : ApplicationAdapter() {

    private var batch: SpriteBatch? = null
    private var bird: Texture? = null

    override fun create() {
        batch = SpriteBatch()
        bird = Texture("passaro1.png")
    }

    override fun render() {
        batch!!.begin()
        batch!!.draw(bird!!, 300f, 50f)
        batch!!.end()
    }

    override fun dispose() {

    }
}