package com.learning.kotlin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Main : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private lateinit var bird: Texture
    private lateinit var background: Texture

    private var birdPositionX = 0f

    private var screenWidth = 0f
    private var screenHeight = 0f

    override fun create() {
        screenWidth = Gdx.graphics.width.toFloat()
        screenHeight = Gdx.graphics.height.toFloat()

        batch = SpriteBatch()
        bird = Texture("passaro1.png")
        background = Texture("fundo.png")
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        batch.draw(background,0f, 0f, screenWidth, screenHeight)
        batch.draw(bird, birdPositionX, 500f)

        birdPositionX += 2.5f

        batch.end()
    }

    override fun dispose() {

    }
}